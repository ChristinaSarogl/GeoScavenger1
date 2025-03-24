								<li class="nav-item">
									<a class="nav-link active" href="home">
										<i class="fs-4 me-2 bi-speedometer2"></i> <span>Dashboard</span>
									</a>
								</li>
								<li class="nav-item">
									<a class="nav-link link-dark" href="create">
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
						<div class="d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pt-3 pb-2 mb-3 border-bottom">
							<h1 class="h2">Dashboard</h1>
						</div>
					  
						<div class="card">
							<div class="card-body text-center">
								<p class="fs-4 mb-2 title">All Hunts</p>
								<hr>
								<p id="noHuntsMessage">You haven't created any hunts yet.</p>

								<div class="table-responsive">
									<table id="hunts-table" class="table table-striped table-sm">
										<thead>
											<tr>
												<th>Hunt Name</th>
												<th>Date</th>
												<th>Players Entered</th>
												<th></th>  
											</tr>
										</thead>
										<tbody>
									
										</tbody>
									</table>
								</div>

								<button id="deleteAll" class="btn btn-red" onclick="deleteAllHunts()">Delete all hunts</button>
								
							</div>
						</div>
						
					</main>				
				</div>
			</div>
		</div>
		
		<div aria-live="polite" aria-atomic="true" class="position-relative">
			<div class="toast-container fixed-bottom p-3" id="toast-container-id" style="z-index: 100">
				
				<div id="active-in-hunt-toast" class="toast fade hide" role="alert" aria-live="assertive" aria-atomic="true">
					<div class="toast-header">
						<strong class="me-auto">Active User Detected</strong>
						<button type="button" class="btn-close" data-bs-dismiss="toast" aria-label="Close"></button>
					</div>
					<div class="toast-body">
						Unable to delete the hunt because an active user was detected.
					</div>
				</div>
				
				<div id="inactive-in-hunt-toast" class="toast fade hide" role="alert" aria-live="assertive" aria-atomic="true">
					<div class="toast-header">
						<strong class="me-auto">Disconnected User Detected</strong>
						<button type="button" class="btn-close" data-bs-dismiss="toast" aria-label="Close"></button>
					</div>
					<div class="toast-body">
						Unable to delete the hunt because a disconnected user was detected. You can try deleting the hunt later.
					</div>
				</div>
				
			</div>
		</div>