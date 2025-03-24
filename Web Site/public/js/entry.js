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

//Listen for auth changes
firebaseAuth.onAuthStateChanged(user => {
    if (!user){
		var path = window.location.pathname;
        var page = path.split("/").pop();
		if(page !== "login" && page !== "register" && page !== "reset-password"){
			window.location.href = "/~1801448/geoscavenger/public/login";
		}			
        console.log("User logged out");
    }
})

//Login user
$(document).ready(function() {
    $(document).on('submit', '#login-form', function(e) {
		const loginForm = document.querySelector('#login-form');

		var email = loginForm['login-email'].value;
        var password = loginForm['login-password'].value;
		
		firebaseAuth.signInWithEmailAndPassword(email,password)
        .then(cred =>{
			var user  = cred.user;
			fireaseFirestore.collection("managers").doc(user.uid).get().then((doc)=>{
                if (doc.exists){
                    window.location.href = "home"; 
                } else {
                    firebaseAuth.signOut();    					
                    document.getElementById('login-error-message').innerHTML="You need a manager account to enter this service!"; 
					document.getElementById('login-error-message').setAttribute('class','mb-3 text-danger');
                }
            })                       
        }).catch((error) => {
            document.getElementById('login-error-message').innerHTML="Incorrect email or password!";
			document.getElementById('login-error-message').setAttribute('class','mb-3 text-danger');
        });
		
		e.preventDefault();	
	});
});

//Register administrator
$(document).ready(function() {
    $(document).on('submit', '#register-form', function(e) {
		const signupForm = document.querySelector('#register-form');
		
		var username = signupForm['register-username'].value;
		var day = signupForm['register-day'].value;
        var month = signupForm['register-month'].value;
        var year = signupForm['register-year'].value;
        var email = signupForm['register-email'].value;
        var password = signupForm['register-password'].value;
        var repeat = signupForm['register-repeat'].value;
		
		
		var age = 0;
		
		//Check if date of birth is valid
		if (parseInt(day) < 1 || parseInt(day) > 31){
			document.getElementById('register-error-message').innerHTML="Invalid date of birth";
		} else if (month < 1 || month > 12){
			document.getElementById('register-error-message').innerHTML="Invalid date of birth";
		} else if (year < (new Date().getFullYear()-100) || year > new Date().getFullYear()) {
			document.getElementById('register-error-message').innerHTML="Invalid date of birth";
		} else {		
			//Calculate age
			var birthday = year + "-" + month + "-" + day;
			var birthdayDate = new Date(birthday);

			age = new Date().getFullYear() - birthdayDate.getFullYear();
			var month= new Date().getMonth() - birthdayDate.getMonth();
			if (month < 0 || (month === 0 && new Date().getDate() < birthdayDate.getDate())){
				age--;
			}
			
			if (password !== repeat){
				document.getElementById('register-error-message').innerHTML="Passwords do not match.";
			} else if(age < 18){
				document.getElementById('register-error-message').innerHTML="You have to be at least 18 to use the service.";
			} else {
				//Register adminisrator
				firebaseAuth.createUserWithEmailAndPassword(email,password)
				.then(cred => {
					var user = cred.user;
					//Change manager's display name
					user.updateProfile({
						displayName: username,
					}).then(() =>{
						//Create firestore document
						fireaseFirestore.collection("managers").doc(user.uid).set({
							username: username,
							photoUrl: null,
							email: email,
							created_hunts: [],
						}).then(() => {
							console.log("User document created!");
							window.location.href = "home";
						})               

					})
				}).catch((error) => {
					var errorCode = error.code;
					if (errorCode === 'auth/weak-password'){
						document.getElementById('register-error-message').innerHTML="Your passoword has to have at least 6 characters";
					} else if(errorCode === 'auth/email-already-in-use'){
						document.getElementById('register-error-message').innerHTML="An account with this email already exists!";
					} else if(errorCode === 'auth/invalid-email'){
						document.getElementById('register-error-message').innerHTML="You have entered an invalid email!";
					} else {
						document.getElementById('register-error-message').innerHTML="Something went wrong!<br>Please check your information!";
					}
				});
			}
		}
		
		e.preventDefault();
	});
});


$(document).ready(function() {
    $(document).on('submit', '#reset-form', function(e) {
		const resetForm = document.querySelector('#reset-form');

        var email = resetForm['reset-email'].value;
		
		firebaseAuth.sendPasswordResetEmail(email).then(() => {
			console.log("Email sent");
			window.location.href = "login";
		}).catch((error) => {
			resetForm['reset-email'].value = "";
			document.getElementById('reset-error-message').style.display = "block";
		});
		
		e.preventDefault();	
	});
});

