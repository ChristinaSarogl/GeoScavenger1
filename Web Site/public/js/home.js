var firebaseConfig = {
    apiKey: "apiKey",
    authDomain: "authDomain",
    databaseURL: "databaseURL",
    projectId: "projectId",
    storageBucket: "storageBucket",
    messagingSenderId: "messagingSenderId",
    appId: "appId",
    measurementId: "measurementId"
};

firebase.initializeApp(firebaseConfig);

//Firebase references
const firebaseAuth = firebase.auth();
const fireaseFirestore = firebase.firestore();

//Create hunt variables
var createMarkerExists = false;
var checkpointMarker;
let createMarkers = [];
let questionsInfo = {};

//Active hunt variables
var usersLocs = {};
var mapView;
var huntPlayersRef;
var huntMessagesRef;
var sendMessagesRef;


//Listen for auth changes
firebaseAuth.onAuthStateChanged(user => {
    if (user){
        loadInfo(user);     
    } else {
		var path = window.location.pathname;
        var page = path.split("/").pop();
		if(page !== "login" && page !== "register" && page !== "reset-password"){
			window.location.href = "/~1801448/geoscavenger/public/login";
		}			
        console.log("User logged out");
    }
})

function logoutUser(){
    firebaseAuth.signOut();
}


//------ HOME PAGE ------

//Load administrators info
function loadInfo(user){
    var storage = firebase.storage();

    fireaseFirestore.collection("managers").doc(user.uid).get().then((doc) => {
        if (doc.get('photoUrl') !== null){
            var imageUrl = doc.get('photoUrl');
            storage.refFromURL(imageUrl).getDownloadURL()
            .then((url)=>{
                document.getElementById('profile-image').setAttribute('src', url);
            });
        } else {
            var pathReference = storage.ref('default_images/avatar_icon.png');
            pathReference.getDownloadURL().then((url) => {
                document.getElementById('profile-image').setAttribute('src', url);
            });
        }

        //Get screen path name
        var path = window.location.pathname;
        var page = path.split("/").pop();

		document.getElementById('username').innerHTML = user.displayName;
		trackAllHunts();
		
        if(page === "home"){
            loadHunts(user);
        } else if (page === "create"){
			var pathReference = storage.ref('default_images/plus.png');
            pathReference.getDownloadURL().then((url) => {
                document.getElementById('add-checkpoint').setAttribute('src', url);
            });
        } else {
			findActiveUsers();
		}
        
    });
}

//Load administrators hunts
function loadHunts(user){
    fireaseFirestore.collection("managers").doc(user.uid).get().then((doc)=>{
        if (doc.get('created_hunts').length !== 0){
            document.querySelector('#noHuntsMessage').style.display = "none";
            var i = 0;
			
            //Display all hunts that the user has created
            doc.get('created_hunts').forEach((doc) => {
                fireaseFirestore.collection("hunts").doc(doc).get().then((huntInfo) =>{
                    var row = document.createElement('tr');

                    var name = document.createElement('td');
                    name.innerHTML = huntInfo.get('name');
					name.setAttribute('onclick',"window.location='active/" + huntInfo.id + "'");

                    var date = document.createElement('td');
                    date.innerHTML = huntInfo.get('date').toDate().toLocaleString('en-US',{
                        day: 'numeric', year: 'numeric', month: 'long'});
					date.setAttribute('onclick',"window.location='active/" + huntInfo.id + "'");

                    var players = document.createElement('td');
                    players.innerHTML = huntInfo.get('players');
					players.setAttribute('onclick',"window.location='active/" + huntInfo.id + "'");

                    var del = document.createElement('td');
                    var string = document.createElement('p');
                    string.innerHTML = "Delete hunt";
                    del.append(string);
                    del.setAttribute('class','clickable');
                    del.setAttribute('onclick','deleteThisHunt(' + i + ')');

                    row.append(name);
                    row.append(date);
                    row.append(players);
                    row.append(del);

                    $('#hunts-table').append(row);
                    i++;
                }); 
				
           });

        } else {
            document.querySelector('#hunts-table').style.display = "none";
            document.querySelector('#deleteAll').style.display = "none";
            document.querySelector('#noHuntsMessage').style.display = "block";
        }

        document.getElementById('loader').style.display="none";
        document.getElementById('main').style.display="block";

    });

}

function trackAllHunts(){
	var databaseRef = firebase.database().ref();
	let notifiedMessages = {};
	
	var path = window.location.pathname;
	var page = path.split("/").pop();
	
	databaseRef.on('child_changed', (data) => {	
		if(page == data.key){
			return;
		}
		
		if (data.val()['players'] != null){
			var players = data.val()['players'];
			var usersIds = Object.keys(players);
			
			for (const userIdIndex in usersIds) {
				var userID = usersIds[userIdIndex];
				playerInfo = players[userID];
				
				//Show popup that some player needs help
				if (playerInfo['HELP'] != undefined){
					
					if(document.getElementById('help-user-toast-' + userID) == null){
						createPopup('help-user-toast-' + userID, "danger", "IMMEDIATE HELP!", playerInfo);				
					} else {
						document.getElementById('help-user-body-'+ userID).innerHTML = playerInfo['name'] + " require immediate help!<br>At location: "
							+ playerInfo['location'].latitude + ", " + playerInfo['location'].longitude + "<br> Hunt: " + playerInfo['hunt_name'];
					}
					
					var toast = new bootstrap.Toast(document.getElementById('help-user-toast-' + userID));
					toast.show();
						
				//Hide the popup
				} else {
					if(document.getElementById('help-user-toast-' + userID) != null){
						var toast = new bootstrap.Toast(document.getElementById('help-user-toast-' + userID));
						toast.hide();
					}
				}
				
				//Check if there are new messages from the user
				if (data.val()['messages'] != null){
					var messages = data.val()['messages'];
					var messagesUserIds = Object.keys(messages);
					
					if (messages[userID] == undefined){
						delete notifiedMessages[userID];
					}
					
					for (const messagesUsersIndex in messagesUserIds){
						var messageUserID = messagesUserIds[messagesUsersIndex];
						if(messageUserID == userID){
							var userMessages = messages[userID];
							userMessagesIds = Object.keys(userMessages);
							
							for (const messageID in userMessagesIds){
								
								if(notifiedMessages[userID] < userMessagesIds.length || notifiedMessages[userID] == undefined){
									console.log("New message");
									notifiedMessages[userID] = userMessagesIds.length;
									if(document.getElementById('new-message-toast-' + userID) == null){										
										createPopup('new-message-toast-' + userID, "message", "New Message!", playerInfo);
									}
									var toast = new bootstrap.Toast(document.getElementById('new-message-toast-' + userID));
									toast.show();									
								}
							}
						}
					}
					
				} else {
					notifiedMessages = {}
				}
			}
		}		
	});
}

function createPopup(popupId, type, popupTitle, playerInfo){
	var toastDiv = document.createElement('div');
	toastDiv.setAttribute('id', popupId);
	toastDiv.setAttribute('role', 'alert');
	toastDiv.setAttribute('aria-live', 'assertive');
	toastDiv.setAttribute('aria-atomic', 'true');
	
	
	var headerDiv = document.createElement('div');
	headerDiv.setAttribute('class','toast-header');
	
	var toastTitle = document.createElement('strong');
	toastTitle.setAttribute('class','me-auto');
	toastTitle.innerHTML = popupTitle;
	
	var toastClose = document.createElement('button');
	toastClose.setAttribute('type', 'button');
	toastClose.setAttribute('class', 'btn-close');
	toastClose.setAttribute('data-bs-dismiss', 'toast');
	toastClose.setAttribute('aria-label', 'Close');
	
	var toastBody = document.createElement('div');	

	if (type == "danger"){
		userID = popupId.split("-")[3];
		toastDiv.setAttribute('class', 'toast fade hide bg-danger');
		toastDiv.setAttribute('data-bs-autohide', 'false');
		
		toastBody.setAttribute('class','toast-body fw-bold');
		toastBody.setAttribute('id','help-user-body-' + userID);
		toastBody.innerHTML = playerInfo['name'] + " require immediate help!<br>At location: "
		+ playerInfo['location'].latitude + ", " + playerInfo['location'].longitude
		+ "<br> Hunt: " + playerInfo['hunt_name'];
		
	} else {
		toastDiv.setAttribute('class', 'toast fade hide');
		
		toastBody.setAttribute('class','toast-body');
		toastBody.innerHTML = playerInfo['name'] + " has sent you a new message!"
			+ "<br> Hunt: " + playerInfo['hunt_name'];
	}
		
	headerDiv.append(toastTitle);
	headerDiv.append(toastClose);
	
	toastDiv.append(headerDiv);
	toastDiv.append(toastBody);	
	
	document.getElementById('toast-container-id').append(toastDiv);
}

function deleteThisHunt(position){		
	var user = firebaseAuth.currentUser;
    fireaseFirestore.collection("managers").doc(user.uid).get().then((doc)=>{
		var huntId = doc.get('created_hunts')[position];
		var databaseRef = firebase.database().ref();	
		databaseRef.child(huntId).get().then((snapshot) => {
			position++;
			
			if (snapshot.exists()) {
				var newPosition = showFailedHuntDelete(snapshot.val()["players"], position, huntId);
				console.log(newPosition);
				if(newPosition == position){
					deleteHunt(user, huntId);
					document.getElementById("hunts-table").deleteRow(position);
				} else {
					position = newPosition;
				}
			} else {		
				deleteHunt(user, huntId);
				document.getElementById("hunts-table").deleteRow(position);

				var delStrings = document.getElementById('hunts-table').getElementsByClassName('clickable');
			
				if (delStrings.length === 0){
					document.querySelector('#hunts-table').style.display = "none";
					document.querySelector('#deleteAll').style.display = "none";
					document.querySelector('#noHuntsMessage').style.display = "block";
				} else {
					for (let entry = 0; entry <= delStrings.length-1; entry++){
						delStrings[entry].setAttribute('onclick', 'deleteThisHunt(' + entry + ')');
					}				
				}
			}
		});
	});
}

function deleteAllHunts(){
	console.log("delete all");
	
	var user = firebaseAuth.currentUser;
	var position = 1;

    //Get all user's hunt ids
    fireaseFirestore.collection("managers").doc(user.uid).get().then((doc)=>{    
        doc.get('created_hunts').forEach((huntId) => {
			
			var databaseRef = firebase.database().ref();	
			databaseRef.child(huntId).get().then((snapshot) => {
				if (snapshot.exists()) {					
					var newPosition = showFailedHuntDelete(snapshot.val()["players"], position, huntId);
					console.log(newPosition);
					if(newPosition == position){
						deleteHunt(user, huntId);
						document.getElementById("hunts-table").deleteRow(position);
					} else {
						position = newPosition;
					}
				} else {
					deleteHunt(user, huntId);
					document.getElementById("hunts-table").deleteRow(position);
				}

				var delStrings = document.getElementById('hunts-table').getElementsByClassName('clickable');
					
				if (delStrings.length === 0){		
					document.querySelector('#hunts-table').style.display = "none";
					document.querySelector('#deleteAll').style.display = "none";
					document.querySelector('#noHuntsMessage').style.display = "block";
				}
			}).catch((error) => {
				console.error(error);
			});	
        });
    });  	
}

function showFailedHuntDelete(players, position, huntId){
	var playersIDs = Object.keys(players);
	
	for (const playerIdIndex in playersIDs){
		playerID = playersIDs[playerIdIndex];
		playerInfo = players[playerID];
		
		if(playerInfo['disconnected'] == true){							
			var lastConnected = playerInfo["last_online"];			
			var split = lastConnected.split(' ');
			
			var dateConnected = split[0].split('-');			
			var timeConnected = split[1].split(':');
			
			var onlineDate = new Date(dateConnected[0],dateConnected[1]-1,dateConnected[2],timeConnected[0],timeConnected[1],'00');
			
			var safeToDelete = false;
			
			if(onlineDate.getFullYear() == new Date().getFullYear()){
				if(onlineDate.getMonth() == new Date().getMonth()){
					if(onlineDate.getDate() != new Date().getDate()){
						safeToDelete = true;
					}
				} else{
					safeToDelete = true;
				}
			} else {
				safeToDelete = true;
			}
			
			if(!safeToDelete){
				var toast = new bootstrap.Toast(document.getElementById('inactive-in-hunt-toast'));
				toast.show();
				
				return (position+1);
			} else {								
				firebase.database().ref(huntId + '/players').child(playerID).remove();
				firebase.database().ref(huntId + '/messages').child(playerID).remove();

				return position;
			}
			
		} else {
			var toast = new bootstrap.Toast(document.getElementById('active-in-hunt-toast'));
			toast.show();
			
			return (position+1);
		}
	}
}	

function deleteHunt(user, huntID){	
	fireaseFirestore.collection("hunts").doc(huntID).get().then((hunt)=>{
		//Get the hunt's checkpoints
		hunt.get('checkpoints').forEach((checkpoint) =>{

			//Delete hunt's checkpoints
			fireaseFirestore.collection('checkpoints').doc(checkpoint).delete().then(() => {
				console.log("Checkpoint successfully deleted!");
			}).catch((error) => {
				console.error("Error removing document: ", error);
			});   
		});
	

		fireaseFirestore.collection("hunts").doc(huntID).delete().then(() => {
			console.log("Hunt successfully deleted!");
		}).catch((error) => {
			console.error("Error removing document: ", error);
		}); 

		fireaseFirestore.collection('managers').doc(user.uid).update({
			created_hunts: firebase.firestore.FieldValue.arrayRemove(huntID)
		});
		console.log("Hunt removed from list!");    
	});
}


//------ CREATE HUNT ------

// Initialize and add the map for create hunt page
function initMapCreate() {
    map = new google.maps.Map(document.getElementById("map"), {
        center: { lat: -34.397, lng: 150.644 },
        zoom: 15,
    });

    if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition((position) => {
            const pos = {
              lat: position.coords.latitude,
              lng: position.coords.longitude,
            };

            map.setCenter(pos);

            map.addListener("click", (mapsMouseEvent) => {
                
                if(createMarkerExists){
                    checkpointMarker.setPosition(mapsMouseEvent.latLng);
                } else {
                    checkpointMarker = new google.maps.Marker({
                        position: mapsMouseEvent.latLng,
                        map,
                        draggable: true
                    });
                    createMarkerExists = true;
                }

            });
        },() => {
            handleLocationError(true);
        });
    } else{
        handleLocationError(false);
    }
	
	document.getElementById('loader').style.display="none";
	document.getElementById('main').style.display="block";
}

function handleLocationError(browserHasGeolocation) {
    if (browserHasGeolocation){
        console.log("Error: The Geolocation service failed.");
    } else {
        console.log("Error: Your browser doesn't support geolocation.");
    }
}

function saveCoordinates(){
	var storage = firebase.storage();
    
	var coordinates = checkpointMarker.getPosition();
    var lat = coordinates.lat();
    var lng = coordinates.lng();
	
	var savedCheckpoints = document.getElementById("checkpoint-list").getElementsByTagName("li").length;
	
	if(savedCheckpoints >= 7){
        document.getElementById('error-message').innerHTML="You can't have more than 7 checkpoints";
    } else {
		var checkpointNumber = savedCheckpoints + 1;
		//Add checkpoint to checkpoint list
        var checkpoint = document.createElement('li');
        checkpoint.setAttribute('id','check' + checkpointNumber);
        checkpoint.setAttribute('class',
            'list-group-item d-flex align-items-center justify-content-between p-2');

        var div = document.createElement('div');        

        var title = document.createElement('p');
        title.innerHTML = "Checkpoint " + checkpointNumber;
        title.setAttribute('class','small-title');

        var coordinates = document.createElement('p');
        coordinates.setAttribute('class','mb-1');
        coordinates.innerHTML = lat.toFixed(6) + ', ' + lng.toFixed(6);

        var deleteIcon = document.createElement('img');
		var pathReference = storage.ref('default_images/bin.png');
            pathReference.getDownloadURL().then((url) => {
                deleteIcon.setAttribute('src',url);
            });
        deleteIcon.setAttribute('class', 'small-image clickable');
        deleteIcon.setAttribute('id','delete' + checkpointNumber);
        deleteIcon.setAttribute('onclick','deleteCheckpoint("'+ checkpointNumber + '")');

        div.append(title);
        div.append(coordinates);
        checkpoint.append(div);
        checkpoint.append(deleteIcon);
        $('#checkpoint-list').append(checkpoint);
		
		//Add checkpoint to challenge list
        var challenge = document.createElement('li');
        challenge.setAttribute('class','list-group-item');
        var title = document.createElement('p');
        title.innerHTML = "Checkpoint " + checkpointNumber;
        title.setAttribute('class','small-title clickable');
        title.setAttribute('onclick', "resetForm("+ checkpointNumber + ")");
        challenge.append(title);
        challenge.setAttribute('id','chal' + checkpointNumber);
        $('#challenge-list').append(challenge);
		
		//Add checkpoint to checkopoint selection
        var selection = document.createElement('option');
        selection.innerHTML = "Checkpoint " + checkpointNumber;
        selection.setAttribute('id','sel' + checkpointNumber);
        $('#challenge-checkpoint').append(selection);
		
		createMarkers.push(checkpointMarker);
        checkpointMarker.setLabel(checkpointNumber.toString());
        createMarkerExists = false;
	}
}

function deleteCheckpoint(checkpointNumber){
	var listIndex = checkpointNumber - 1;
	
	//Remove element from checkpoint list
    var checkEl = document.getElementById('check' + checkpointNumber);
    checkEl.remove();

    //Remove element from challenge list
    var chalEl = document.getElementById('chal' + checkpointNumber);
    chalEl.remove();

    //Remove element from selection list
    var selEl = document.getElementById('sel' + checkpointNumber);
    selEl.remove();
	
	//Remove marker
	var removedMarker = createMarkers.splice(listIndex,1);
	removedMarker[0].setMap(null);
	for (var marker = 0; marker < createMarkers.length; marker++){
		createMarkers[marker].setLabel((marker+1).toString());
	}
	
	const checkpointItems = document.getElementById('checkpoint-list').getElementsByTagName('li');
    const challengeItems = document.getElementById('challenge-list').getElementsByTagName('li');
	
	for (let item = 0; item <= checkpointItems.length - 1; item++) {
		var newNumber = item + 1;
        //Change challenge list
        var challenge = challengeItems[item].getElementsByClassName('small-title');
        for(let i = 0; i <= challenge.length-1; i++){
            challenge[i].innerHTML = "Checkpoint " + newNumber;
            challenge[i].setAttribute('onclick', "resetForm(" + newNumber + ")");
        }
        challengeItems[item].setAttribute('id','chal' + newNumber);
		
		//Change checkpoint list
        checkpointItems[item].setAttribute('id','check' + newNumber);

        var title = checkpointItems[item].getElementsByClassName('small-title');
        for(let i = 0; i <= title.length-1; i++){
            title[i].innerHTML = "Checkpoint " + newNumber;
        }

        var image = checkpointItems[item].getElementsByClassName('small-image');
        for(let i = 0; i <= image.length-1; i++){
            image[i].setAttribute('id','delete' + newNumber);
            image[i].setAttribute('onclick','deleteCheckpoint("'+ newNumber + '")');
        }
	}
	
	const selectionItems = document.getElementById('challenge-checkpoint').getElementsByTagName('option');

    //Change selection list
    for(let sel = 0; sel <= selectionItems.length-1; sel++){
        if (sel !== 0){
            selectionItems[sel].setAttribute('id','sel'+ sel);
            selectionItems[sel].innerHTML = "Checkpoint " + sel;
        }
    } 
	
	//Check if any questions are saved and delete them	
	if (Object.keys(questionsInfo).length !== 0){
		var index = parseInt(checkpointNumber);
		delete questionsInfo['Checkpoint '+ index];    
		let newOrder = {};
		for (const checkpoint in questionsInfo) {
			var checkpointKey = checkpoint.split(" ");
			var newNumber = parseInt(checkpointKey[1]);
			if(newNumber > checkpointNumber){
				newNumber--;
				newOrder['Checkpoint ' + newNumber] = questionsInfo[checkpoint];
			} else {
				newOrder[checkpoint] = questionsInfo[checkpoint];
			}
		}
		questionsInfo = newOrder;
	}
}

//Save checkpoint's question
$(document).ready(function() {
    $(document).on('submit', '#question-form', function(e) {
        const questionForm = document.querySelector('#question-form');
        const answers = [];
        
        const checkpointQuestionInfo = {};
        var checkpoint = questionForm['challenge-checkpoint'].value;
        var index = questionForm['challenge-checkpoint'].selectedIndex;
        
		var clue = questionForm['checkpoint-clue'].value;
        var question = questionForm['challenge-question'].value;
		
        checkpointQuestionInfo.question = question;
		checkpointQuestionInfo.clue = clue;

        for(let answerNo = 0; answerNo < 4; answerNo++){
            var answer = questionForm['answer' + (answerNo + 1)].value;
            answers[answerNo] = answer;
        }
        checkpointQuestionInfo.answers = answers;

        var rightAnswerIndex = questionForm['answer'].value;
        checkpointQuestionInfo.rightAnswerIndex = rightAnswerIndex;
        
		if (!questionsInfo.hasOwnProperty(checkpoint)){
            questionsInfo[checkpoint] = checkpointQuestionInfo;
        } else {
            questionsInfo[checkpoint] = checkpointQuestionInfo;
        }
		
        document.getElementById('chal' + index).style.backgroundColor = "#5BA6A2";
        var p = document.getElementById('chal' + index).getElementsByTagName('p');
        for (let i = 0; i <= p.length-1; i++){
            p[i].style.color = 'white';
        }
		
        questionForm.reset();

        e.preventDefault();
    });
});

//Clear form of load saved information
function resetForm(checkpointNumber){
    const questionForm = document.querySelector('#question-form');
    
    if (Object.keys(questionsInfo).length !== 0){
        var qDetails = questionsInfo['Checkpoint ' + checkpointNumber];
        //If the question is already saved, load
        if (typeof qDetails !== 'undefined'){
            document.querySelector('#challenge-question').value = qDetails.question;
			document.querySelector('#checkpoint-clue').value = qDetails.clue;
            document.querySelector('#answer1').value = qDetails.answers[0];
            document.querySelector('#answer2').value = qDetails.answers[1];
            document.querySelector('#answer3').value = qDetails.answers[2];
            document.querySelector('#answer4').value = qDetails.answers[3];
            document.querySelector('#radio' + qDetails.rightAnswerIndex).checked = true;
        } else {
            questionForm.reset();
        }
    } else {
        questionForm.reset();
    }

    document.querySelector('#challenge-checkpoint').options.item(checkpointNumber).selected = "selected";
}

//Save hunt
$(document).ready(function() {
    $(document).on('submit', '#create-form', function(e) {
        const createForm = document.querySelector('#create-form');
        const checkpointsIDs = [];

        var name = createForm['hunt-name'].value;
        var huntName = name;
        
        var checkpoints = document.getElementById('checkpoint-list').getElementsByTagName('li');
		
        if (checkpoints.length < 3){
			document.getElementById('create-error-message').style.display = "block";
            document.getElementById('create-error-message').innerHTML="You have to add at least 3 checkpoints to the hunt!";
			window.scrollTo(0, 0);
        } else if(checkpoints.length !== Object.keys(questionsInfo).length){
			document.getElementById('create-error-message').style.display = "block";
            document.getElementById('create-error-message').innerHTML="You have to enter a question for each checkpoint!";
			window.scrollTo(0, 0);
        } else{
            document.getElementById('loader').style.display="block";
            document.getElementById('main').style.display="none";
			
			for (let checkp = 1; checkp <= checkpoints.length; checkp ++){

                //Save all the questions details
                var questionDetails  = questionsInfo['Checkpoint '+ checkp];
				var clue = questionDetails['clue'];
                var question = questionDetails['question'];
                var rightAnswer = questionDetails['rightAnswerIndex'];
                var answers = questionDetails['answers'];

                //Get checkpoint's coordinates
                var checkCoord = document.querySelector('#check' + checkp).getElementsByTagName('p')[1].textContent;
                const coordinatesArray = checkCoord.split(", ");
                var lat = parseFloat(coordinatesArray[0]);
                var lng = parseFloat(coordinatesArray[1]);
				
				//Save checkpoint information
                var checkpointRef = fireaseFirestore.collection('checkpoints').doc();
                
                checkpointRef.set({
                    location: new firebase.firestore.GeoPoint(lat,lng),
					clue: clue,
                    question: question,
                    rightAnswerIndex: parseInt(rightAnswer),
                    0: answers[0],
                    1: answers[1],
                    2: answers[2],
                    3: answers[3]
                })
                
                checkpointsIDs[checkp-1] = checkpointRef.id;
			}
			
			var firebaseHuntRef = fireaseFirestore.collection('hunts').doc();

            firebaseHuntRef.set({
                name: huntName,
                players: 0,
                date: new Date(),
                checkpoints: checkpointsIDs
            });

            var huntID = firebaseHuntRef.id;
            var user = firebaseAuth.currentUser;

            //Add hunt to user's collection
            fireaseFirestore.collection('managers').doc(user.uid).update({
                created_hunts: firebase.firestore.FieldValue.arrayUnion(huntID)
            }).then(() =>{
                window.location.href = "home"; 
            });
        }
		
        e.preventDefault();
    })
});


//------ HUNT INFO ------

// Initialize and add the map for active hunt page
function activeMapView() {
    mapView = new google.maps.Map(document.getElementById("map"), {
        center: { lat: -34.397, lng: 150.644 },
        zoom: 15,
    });

    if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition((position) => {
            const pos = {
              lat: position.coords.latitude,
              lng: position.coords.longitude,
            };

            mapView.setCenter(pos);

        },() => {
            handleLocationError(true);
        });
    } else{
        handleLocationError(false);
    }
	
	document.getElementById('loader').style.display="none";
	document.getElementById('main').style.display="block";
}

function openMap(){
	document.getElementById('map-container').style.display = "block";
	document.getElementById('messages-container').style.display = "none";

	document.getElementById('map-toggle').setAttribute('class','nav-link active me-1');
	document.getElementById('map-toggle').style.backgroundColor = null;
	document.getElementById('messages-toggle').setAttribute('class','nav-link link-dark position-relative');
	document.getElementById('messages-toggle').style.backgroundColor = "#cf9a5e1f";
}

function openMessages(){
	document.getElementById('map-container').style.display = "none";
	document.getElementById('messages-container').style.display = "block";

	document.getElementById('messages-toggle').setAttribute('class','nav-link position-relative active');
	document.getElementById('messages-toggle').style.backgroundColor = null;
	document.getElementById('messages-notification').style.display = "none";
	document.getElementById('map-toggle').setAttribute('class','nav-link link-dark me-1');
	document.getElementById('map-toggle').style.backgroundColor = "#cf9a5e1f";
}

function findActiveUsers(){
	var idString = document.querySelector('#active-hunt-id').innerHTML;
	var huntID = idString.split(" ")[1];
	var trackingMessages = false;
	
	fireaseFirestore.collection("hunts").doc(huntID).get().then((huntInfo) =>{
		document.getElementById('hunt-name').innerHTML= huntInfo.get('name');
	});
	document.getElementById('no-players-message').style.display = "block";
	document.getElementById('map').style.display = "none";
	document.getElementById('messages-toggle').disabled = true;
	
	huntPlayersRef = firebase.database().ref(huntID + '/players');
	huntPlayersRef.on('value',(snapshot) => {	
		if (snapshot.val() !== null){
			document.getElementById('no-players-message').style.display = "none";
			document.getElementById('map').style.display = "block";
			document.getElementById('messages-toggle').disabled = false;
			
			//Track messages
			if(!trackingMessages){
				trackMessages(huntID);
				trackingMessages = true;
			}
		}
	});
	
	//New user entered
	huntPlayersRef.on('child_added', (data) => {
		var name = Object.values(data.val()["name"]).join('');
		
		//Add user to chat
		var chatUsers = document.getElementById('chat-users');
		var newUser = document.createElement('button');
		newUser.setAttribute('class','btn btn-green mb-1 py-1 text-start w-100');	
		newUser.setAttribute('id', 'chat-button-' + data.key + '-' + huntID);
		newUser.setAttribute('onclick', "loadMessages('" + huntID + "','" + name + "','" + data.key + "'," + false + "," + null + ")");
		newUser.innerHTML = name;
		
		chatUsers.append(newUser);
		
		if (data.val()["location"] !== undefined){
			updateUserLocation(data.key, name, data.val()["location"].latitude, data.val()["location"].longitude);
			if(data.val()["HELP"] !== undefined){
				displayUserDanger(data.key, name, data.val()["location"].latitude, data.val()["location"].longitude);
			}
		}
		
		if (data.val()["disconnected"] !== undefined){
			if (data.val()["disconnected"]){
				setDisconnected(huntID, name, data.key, data.val()["last_online"]);					
			} else {
				setConnected(huntID, name, data.key);
			}
		}
	});
	
	//Update user info
	huntPlayersRef.on('child_changed', (data) => {
		var name = Object.values(data.val()["name"]).join('');
		
		if(data.val()["location"] !== undefined){			
			if(data.val()["HELP"] !== undefined){
				displayUserDanger(data.key, name, data.val()["location"].latitude, data.val()["location"].longitude);
			} else {
				//Check if there is a danger alert for this user
				deleteDangerAlert(data.key);
			}
			
			updateUserLocation(data.key, name, data.val()["location"].latitude, data.val()["location"].longitude);
		}
		
		if(data.val()["disconnected"] !== undefined){
			if (data.val()["disconnected"]){
				setDisconnected(huntID, name, data.key, data.val()["last_online"]);				
			} else {
				setConnected(huntID, name, data.key);
			}
		}	
		
	});
	
	//User exited hunt
	huntPlayersRef.on('child_removed',(data) => {
		//Remove marker on map
		usersLocs[data.key].setMap(null);
		delete usersLocs[data.key];
		
		//Remove user chat button
		document.getElementById('chat-button-' + data.key + '-' + huntID).remove();
		
		//Check if the chat is open
		if(document.getElementById('chat-container').style.display == "block"){
			var titleElements = document.getElementById('chat-title').getElementsByTagName('span');
			var titleUsername = titleElements[1].id.split("-");
			if(titleUsername[2] == data.key){
				document.getElementById('chat-container').style.display = "none";
			}
		}
	});
	
	//Hunt has no active players
	var huntRef = firebase.database().ref(huntID);
	huntRef.on('child_removed', (data) => {
		if (data.key === "players"){	
			//Remove all messages 
			huntRef.child('messages').remove();
			
			//Reset UI
			document.getElementById('no-players-message').style.display = "block";
			document.getElementById('map').style.display = "none";
			document.getElementById('messages-toggle').disabled = true;
			document.getElementById('chat-users').innerHTML = "";
			document.getElementById('chat-container').style.display = "none";
			document.getElementById('chat-title').innerHTML = "";			
			
			openMap();
		}
	});	
}

function updateUserLocation(userID, name, latitude, longitude){
	marker = usersLocs[userID];
	
	if (marker == undefined){	
		const pos = { lat: latitude, lng: longitude }
		
		userLocation = new google.maps.Marker({
			position: pos,
			map: mapView,
			draggable: false,
			title: name
		});
	
		mapView.setCenter(pos);
		
		usersLocs[userID] = userLocation;
		addInfoWindow(userID, name);
	} else {		
		marker.setPosition({lat: latitude, lng: longitude});
	}	
}

function addInfoWindow(userID, message){
	usersLocs[userID]['infoWindow'] = new google.maps.InfoWindow({
		content: message
	});
	
	usersLocs[userID]['infoWindow'].open(mapView,usersLocs[userID]);
	
	google.maps.event.addListener(usersLocs[userID], 'click', function(){
		this['infoWindow'].open(mapView, usersLocs[userID]);
	});
}

function displayUserDanger(userID, name, latitude, longitude){
	var alertPlaceholder = document.getElementById('liveAlertPlaceholder');
	
	if(document.getElementById('alert-location-' + userID) == null){
		var alertWrapper = document.createElement('div');
		alertWrapper.setAttribute('class','alert alert-danger alert-dismissible text-center');
		alertWrapper.setAttribute('id', 'alert-' + userID);
		alertWrapper.setAttribute('role','alert');
		
		var alertTitle = document.createElement('div');
		alertTitle.setAttribute('class','d-flex align-items-center justify-content-center');		
		
		var iconDanger = document.createElement('i');
		iconDanger.setAttribute('class', 'bi-exclamation-triangle-fill fs-4');
		
		var userName = document.createElement('p');
		userName.setAttribute('class','m-0 ps-2');
		userName.innerHTML = name + " in danger!";
		
		var userLocation = document.createElement('p');
		userLocation.setAttribute('class','fs-6 mb-2');
		userLocation.setAttribute('id','alert-location-' + userID);
		userLocation.innerHTML = "Location: " + latitude + ", " + longitude;	
		
		var closeBtn = document.createElement('button');
		closeBtn.setAttribute('type','button');
		closeBtn.setAttribute('class','btn-close');
		closeBtn.setAttribute('data-bs-dismiss','alert');
		closeBtn.setAttribute('aria-label','Close');
		
		alertTitle.append(iconDanger);
		alertTitle.append(userName);		
		alertWrapper.append(alertTitle);
		alertWrapper.append(userLocation);
		alertWrapper.append(closeBtn);

		alertPlaceholder.append(alertWrapper);
	} else {
		elementId = "alert-location-" + userID;
		locationAlert = document.getElementById(elementId);
		locationAlert.innerHTML = "Location: " + latitude + ", " + longitude;
	}	
}

function deleteDangerAlert(userId){
	if(document.getElementById('alert-' + userId) != null){
		document.getElementById('alert-' + userId).remove();
	}
}

function setDisconnected(huntID, name, userID, lastOnline){
	document.getElementById('chat-button-' + userID + '-' + huntID).style.backgroundColor = "grey";
	document.getElementById('chat-button-' + userID + '-' + huntID).setAttribute('onclick', 
		"loadMessages('" + huntID + "','" + name + "','" + userID + "'," + true + ",'" + lastOnline + "')");
		
	if(document.getElementById('chat-active-dot-' + userID) != null){
		document.getElementById('chat-active-dot-' + userID).setAttribute('class','bg-danger active-dot');
	}
			
	if(document.getElementById('chat-last-online-' + userID) != null){	
		document.getElementById('chat-last-online-div').style.display = "inline-block";
		document.getElementById('chat-last-online-' + userID).innerHTML = "Last online: " + lastOnline;	
		document.getElementById('chat-remove-dropdown').style.display = "inline-block";
		document.getElementById('chat-remove-button').setAttribute('onclick','removeUser("' + huntID + '","' + userID + '")');
	}
			
	
}

function setConnected(huntID, name, userID){
	document.getElementById('chat-button-' + userID + '-' + huntID).style.backgroundColor = null;
	document.getElementById('chat-button-' + userID + '-' + huntID).setAttribute('onclick', 
		"loadMessages('" + huntID + "','" + name + "','" + userID + "'," + false + "," + null + ")");
		
	if(document.getElementById('chat-active-dot-' + userID) != null){
		document.getElementById('chat-active-dot-' + userID).setAttribute('class','bg-success active-dot');
	}
	
	if(document.getElementById('chat-last-online-' + userID) != null){	
		document.getElementById('chat-last-online-div').style.display = "none";
		document.getElementById('chat-remove-dropdown').style.display = "none";
	}
	
	
}

function trackMessages(huntID){
	huntMessagesRef = firebase.database().ref(huntID + '/messages/');
	
	huntMessagesRef.on('child_added', (data) => {
		
		var messageInfo = data.val()[Object.keys(data.val())[0]];
		
		var messageTab = document.getElementById('messages-toggle').className;
		
		if(messageTab != "nav-link position-relative active"){
			document.getElementById('messages-notification').style.display = "block";
		} 
		
		var chatTitleSpan = document.getElementById('chat-title').getElementsByTagName('span');

		if(chatTitleSpan[1] === undefined){
			if(messageInfo['type'] == 'personal'){
				var userButton = document.getElementById('chat-button-' + data.key + '-' + huntID);
				
				var badge = document.createElement('span');
				badge.setAttribute('class','badge bg-danger ms-2');
				badge.setAttribute('id','span-' + data.key);
				badge.innerHTML = 1;
				
				userButton.append(badge);
			}
		} else {
			var titleUserID = chatTitleSpan[1].id.split('-');
			
			if(titleUserID[2] !== data.key){
				if(messageInfo['type'] == 'personal'){
					var userButton = document.getElementById('chat-button-' + data.key + '-' + huntID);
				
					var badge = document.createElement('span');
					badge.setAttribute('class','badge bg-danger ms-2');
					badge.setAttribute('id','span-' + data.key);
					badge.innerHTML = 1;
					
					userButton.append(badge);
				}
			} else {					
				addMessageToChat(messageInfo);
			}
		}		
		
	});
	
	huntMessagesRef.on('child_changed', (data) => {
		
		var messageInfo = data.val()[Object.keys(data.val())[Object.keys(data.val()).length - 1]];
			
		var messageTab = document.getElementById('messages-toggle').className;
		
		if(messageTab != "nav-link position-relative active"){
			document.getElementById('messages-notification').style.display = "block";
		}
		
		var chatTitleSpan = document.getElementById('chat-title').getElementsByTagName('span');
		var userBadge = document.getElementById('span-' + data.key);

		if(chatTitleSpan[1] === undefined){
			if(messageInfo['type'] == 'personal'){
				if(userBadge !== null){
					var number = userBadge.innerHTML;				
					number++;
					
					userBadge.innerHTML = number;
					
					
				} else {
					var userButton = document.getElementById('chat-button-' + data.key + '-' + huntID);
					
					var badge = document.createElement('span');
					badge.setAttribute('class','badge bg-danger ms-2');
					badge.setAttribute('id','span-' + data.key);
					badge.innerHTML = 1;
					
					userButton.append(badge);
				}
			}
			
		} else {
			var titleUserID = chatTitleSpan[1].id.split('-');
			
			if(titleUserID[2] !== data.key){
				if(messageInfo['type'] == 'personal'){
					if(userBadge !== null){
						var number = userBadge.innerHTML;				
						number++;
						
						userBadge.innerHTML = number;
						
					} else {
						var userButton = document.getElementById('chat-button-' + data.key + '-' + huntID);
						
						var badge = document.createElement('span');
						badge.setAttribute('class','badge bg-danger ms-2');
						badge.setAttribute('id','span-' + data.key);
						badge.innerHTML = 1;
						
						userButton.append(badge);
					}
				}
			} else {
				addMessageToChat(messageInfo);
			}
		}			
		
	});
}

function loadMessages(huntID, username, userID, disconnected, lastOnline){	
	var messageList = document.getElementById('messages-list');
	messageList.innerHTML = "";
	
	var lastOnlineDiv = document.getElementById('chat-last-online-div');
	lastOnlineDiv.innerHTML = "";
	
	//Remove badge
	var userBadge = document.getElementById('span-' + userID);
	if(userBadge !== null){
		userBadge.remove();
	}	
	
	sendMessagesRef = firebase.database().ref(huntID + "/messages/" + userID);
	
	firebase.database().ref().child(huntID).child("messages").child(userID).get().then((snapshot) => {
		if (snapshot.exists()) {
			var messages = snapshot.val();
			
			for(let key in messages){
				var messageInfo = messages[key];
				addMessageToChat(messageInfo);				
			}
			
		} else {
			console.log("No data available");
		}
	}).catch((error) => {
		console.error(error);
	});
	
	
	var smallLastOnline = document.createElement('small');
	smallLastOnline.setAttribute('id','chat-last-online-' + userID);
	smallLastOnline.innerHTML = "Last online: " + lastOnline;
	lastOnlineDiv.append(smallLastOnline);

	var activeDot;
	
	if(disconnected){
		activeDot = "<span class='bg-danger active-dot' id='chat-active-dot-" + userID + "'></span>";
		
		lastOnlineDiv.style.display = "inline-block";	
		
		document.getElementById('chat-remove-dropdown').style.display = "inline-block";
		document.getElementById('chat-remove-button').setAttribute('onclick','removeUser("' + huntID + '","' + userID + '")');
	} else {
		activeDot = "<span class='bg-success active-dot' id='chat-active-dot-" + userID + "'></span>";
		
		document.getElementById('chat-last-online-div').style.display = "none";
		document.getElementById('chat-remove-dropdown').style.display = "none";
	}
	
	document.getElementById('chat-title').innerHTML = activeDot + "<span class='ps-2' id='chat-username-" + userID + "'>" + username + "</span>";	
	document.getElementById('chat-container').style.display = "block";
}

function addMessageToChat(messageInfo){
	var messageList = document.getElementById('messages-list');
	
	var message = messageInfo["text"];

	var messageWrapper = document.createElement('li');
	messageWrapper.setAttribute('class','mb-2 text-start');
	
	var messageText = document.createElement('p');
	messageText.setAttribute('class','message d-inline-block rounded p-2 m-0');
	messageText.innerHTML = message;
	
	var sender = document.createElement('small');
	sender.setAttribute('class','d-block text-secondary');
	
	if (messageInfo['type'] == 'personal'){
		sender.innerHTML = messageInfo["name"];
	} else {
		sender.innerHTML = messageInfo["name"] + " | Group";
	}
	
	messageWrapper.append(messageText);		
	messageWrapper.append(sender);		
	messageList.append(messageWrapper);

	const scrollToBottom = (node) => {
		node.scrollTop = node.scrollHeight;
	}

	scrollToBottom(messageList);
}

//Send personal message
$(document).ready(function() {
    $(document).on('submit', '#message-form', function(e) {
		var messageInput = document.getElementById("chat-message-input");
		var message = messageInput.value;
		
		if(message !== ""){
			const msg = {
				name: "Admin",
				text: message,
				type: "personal"
			};
			
			sendMessagesRef.push(msg);
		}		
		
		messageInput.value = "";
		
		e.preventDefault();
	});
});

//Send mass message
$(document).ready(function() {
    $(document).on('submit', '#send-to-all-form', function(e) {
		var message = document.getElementById("message-to-all-input").value;
		
		if(message !== ""){
			const msg = {
				name: "Admin",
				text: message,
				type: "group"
			};
			
			huntPlayersRef.get().then((snapshot) => {
				if (snapshot.exists()) {
					var players = snapshot.val();
					
					for(let key in players){
						huntMessagesRef.child(key).push(msg);						
					}
					
				} else {
					console.log("No data available");
				}
			}).catch((error) => {
				console.error(error);
			});
	
			var toast = new bootstrap.Toast(document.getElementById('message-to-all-toast'));
			toast.show();
		}	
		
		document.getElementById("message-to-all-input").value = "";
		e.preventDefault();
	});
});

function removeUser(huntId, userId){	
	const huntPlayerRef = firebase.database().ref(huntId + '/players').child(userId).get().then((snapshot) => {
		if (snapshot.exists()) {		
			var lastConnected = snapshot.val()["last_online"];			
			var split = lastConnected.split(' ');
			
			var dateConnected = split[0].split('-');			
			var timeConnected = split[1].split(':');
			
			var onlineDate = new Date(dateConnected[0],dateConnected[1]-1,dateConnected[2],timeConnected[0],timeConnected[1],'00');
			
			var safeToDelete = false;
			
			if(onlineDate.getFullYear() == new Date().getFullYear()){
				if(onlineDate.getMonth() == new Date().getMonth()){
					if(onlineDate.getDate() != new Date().getDate()){
						safeToDelete = true;
					}
				} else{
					safeToDelete = true;
				}
			} else {
				safeToDelete = true;
			}
			
			if(!safeToDelete){
				var toast = new bootstrap.Toast(document.getElementById('unable-to-delete-toast'));
				toast.show();
			} else {
				firebase.database().ref(huntId + '/players').child(userId).remove();
				firebase.database().ref(huntId + '/messages').child(userId).remove();	

				var toast = new bootstrap.Toast(document.getElementById('deleted-user-toast'));
				toast.show();
				
			}
			
		} else {
			console.log("No data available");
		}
	}).catch((error) => {
		console.error(error);
	});
}