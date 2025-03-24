								<li class="nav-item">
									<a class="nav-link link-dark" href="<?php echo base_url(); ?>/home">
										<i class="fs-4 me-2 bi-speedometer2"></i> <span>Dashboard</span>
									</a>
								</li>
								<li class="nav-item">
									<a class="nav-link link-dark" href="<?php echo base_url(); ?>/create">
										<i class="fs-4 me-2 bi-pin-map"></i> <span>Create hunt</span>
									</a>
								</li>
							</ul>
							<hr>
							<a class="nav-link link-danger" href="#" onclick="logoutUser()">
								<i class="fs-4 bi-box-arrow-right"></i> <span>Logout</span>
							</a>
						</div>
					</nav>

					<main class="col-md-9 ms-sm-auto col-lg-10 px-md-4">						
						<div class="d-flex flex-column flex-wrap flex-md-nowrap pt-3 pb-2 mb-3 border-bottom">
							<h1 class="h3" id="hunt-name"></h1>
							<p  class="mb-2" id="active-hunt-id">ID: <?= esc($huntId) ?></p>
						</div>
						
						<div id="liveAlertPlaceholder"></div>
						
						<ul class="nav nav-tabs">
							<li class="nav-item">
								<button class="nav-link active me-1" id="map-toggle" onclick="openMap()">Map</button>
							</li>
							<li class="nav-item">
								<button class="nav-link link-dark position-relative" id="messages-toggle" onclick="openMessages()" style="background-color:#cf9a5e1f;">
									Hunt Messages
									<span class="position-absolute top-0 start-100 translate-middle p-2 bg-danger border border-light rounded-circle"
										id="messages-notification" style="display: none;"></span>
								</button>
							</li>
						</ul>
						
						<div class="bg-white px-3 py-2 mb-4 border-start border-bottom border-end" id="map-container">
							<p id="no-players-message">There are no active players at right now.</p>

							<div class="bg-success" id="map"  style="height: 500px;"></div>                
						</div>
						
						<div class="bg-white px-3 py-2 mb-4 border-start border-bottom border-end" id="messages-container" style="display: none;">
							<div class="row">

								<div class="col-md-5 col-lg-4 p-0">
									<div class="text-start h-100 rounded-start" style="background-color:#5BA6A2">
										<p class="ps-3 pt-2 mb-2 text-white">Active users</p>
										
										<div class="ms-2 me-5 ps-3" id="chat-users"></div>
										
										<div class="my-2 text-center">
											<button type="button" class="btn btn-outline-light" data-bs-toggle="modal" data-bs-target="#message-to-all">
												Message to all
											</button>
										</div>
										
									</div>
								</div>
								
								<div class="col-md-7 col-lg-8 p-0" id="chat-container" style="height: 500px; display: none;">
									<div class="d-flex align-items-center justify-content-between" style="background-color: #dfdfdf;">
										<p class="m-0 py-2 ps-3" id ="chat-title"></p>
										
										<div class="dropdown">
											<div class="text-secondary" id="chat-last-online-div"></div>
											<button class="btn btn-outline-secondary btn-sm me-2" type="button" id="chat-remove-dropdown"
												data-bs-toggle="dropdown" aria-expanded="false">
												<i class="bi bi-three-dots-vertical"></i>
											</button>
											<ul class="dropdown-menu" aria-labelledby="chat-remove-dropdown">
												<li><button class="dropdown-item" id="chat-remove-button">Remove User</button></li>
											</ul>
										</div>
									</div>
									
									<hr class="m-0">
									<ul class="py-2 ps-3 m-0 bg-white h-100" id="messages-list">                                      
									</ul>
									
									<form id="message-form">
										<div class="d-flex align-items-center justify-content-center py-2" style="background-color: #dfdfdf;">
											<div class="col-8 col-md-8 col-lg-9 me-2">
												<input type="text" class="form-control" id="chat-message-input">
											</div>
											<div class="col-auto">
												<label for="chat-message-submit" class="btn btn-green"><i class="bi bi-send"> Send</i></label>
												<input class="visually-hidden" id="chat-message-submit" type="submit" value=""/>
											</div>										
										</div>
									</form>
								</div>
								
							</div>
						</div>
						
					</main>
				</div>
			</div>
		</div>
		
		<div class="modal fade" id="message-to-all" tabindex="-1" aria-hidden="true">
			<div class="modal-dialog">
				<div class="modal-content">
				
					<div class="modal-header">
						<h5 class="modal-title">Message to all users</h5>
						<button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
					</div>
					
					<div class="modal-body">
						<form id="send-to-all-form">
							<input type="text" class="form-control" id="message-to-all-input" placeholder="Your message">
							
							<div class="mt-2 text-end">
								<label for="message-to-all-button" class="btn btn-green"><i class="bi bi-send"> Send to all</i></label>
								<input class="visually-hidden" id="message-to-all-button" type="submit" value="" data-bs-dismiss="modal"/>
							</div>
						</form>
					</div>
					
				</div>
			</div>
		</div>
		
		<div aria-live="polite" aria-atomic="true" class="position-relative">
			<div class="toast-container fixed-bottom p-3" id="toast-container-id" style="z-index: 100">
				
				<div id="message-to-all-toast" class="toast fade hide" role="alert" aria-live="assertive" aria-atomic="true">
					<div class="toast-header">
						<strong class="me-auto">GeoScavenger</strong>
						<button type="button" class="btn-close" data-bs-dismiss="toast" aria-label="Close"></button>
					</div>
					<div class="toast-body">
						The message was sent to all users.
					</div>
				</div>
				
				<div id="unable-to-delete-toast" class="toast fade hide" role="alert" aria-live="assertive" aria-atomic="true">
					<div class="toast-header">
						<strong class="me-auto">GeoScavenger</strong>
						<button type="button" class="btn-close" data-bs-dismiss="toast" aria-label="Close"></button>
					</div>
					<div class="toast-body">
						The user have to be offline for at least 1 day, to remove them from the hunt.
					</div>
				</div>
				
				<div id="deleted-user-toast" class="toast fade hide" role="alert" aria-live="assertive" aria-atomic="true">
					<div class="toast-header">
						<strong class="me-auto">Removed User</strong>
						<button type="button" class="btn-close" data-bs-dismiss="toast" aria-label="Close"></button>
					</div>
					<div class="toast-body">
						User was removed from the hunt.
					</div>
				</div>
				
			</div>
		</div>
		
		<script async
			src="https://maps.googleapis.com/maps/api/js?key=apiKey&callback=activeMapView">
		</script>
	