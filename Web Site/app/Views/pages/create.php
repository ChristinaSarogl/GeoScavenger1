								<li class="nav-item">
									<a class="nav-link link-dark" href="home">
										<i class="fs-4 me-2 bi-speedometer2"></i> <span>Dashboard</span>
									</a>
								</li>
								<li class="nav-item">
									<a class="nav-link active" href="create">
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
							<h1 class="h2">Create Hunt</h1>
						</div>
					  
						<div class="card mb-4">
							<div class="card-body text-center py-3">              
								<form id="create-form"></form>
								<form id="question-form"></form>

								<p class="mb-3 text-danger" id="create-error-message" style="display:none;"></p>

								<div class="input-group">
									<div class="input-group-text">Hunt name</div>
									<input type="text" class="form-control" id="hunt-name" placeholder="Hunt Name" form="create-form" required>
								</div>
								<hr>

								<div class="row px-3 text-start">
									<div class="col-md-5 order-2 order-sm-2 px-2">
										<div class="d-flex align-items-center justify-content-between">
											<p class="fw-bold small-title">CHECKPOINTS</p>
											<img class="small-image" id="add-checkpoint" src=""
												alt="Add checkpoint" onclick="saveCoordinates()"/>
										</div> 
										<ul class="list-group list-group-flush" id="checkpoint-list"></ul>                       
									</div>
									
									<div class="col-md-7 order-1 order-sm-1 p-0">
										<div id="map"></div>
									</div>
								</div>

								<hr>

								<div class="row px-3">
									<div class="col-md-5 col-lg-4 order-1 order-sm-1 text-start">
										<p class="fw-bold small-title">CHALLENGES</p>

										<ul class="list-group list-group-flush" id="challenge-list"></ul>
									</div>

									<div class="col-md-7 col-lg-8 order-2 order-sm-2 mt-3 text-center">
										<div class="input-group input-group-sm mb-3">
											<label class="input-group-text" for="challenge-checkpoint">Checkpoint</label>
											<select class="form-select" id="challenge-checkpoint" form="question-form" required>
												<option value="">Choose checkpoint</option>
											</select>
										</div>

										 <div class="mb-3 text-start">
											<label for="checkpoint-clue" class="form-label fs-6">Checkpoint clue</label>
											<textarea class="form-control" id="checkpoint-clue" rows="4" form="question-form" required></textarea>
										</div>
										
										<div class="input-group input-group-sm mb-3">
											<span class="input-group-text">Question</span>
											<input type="text" class="form-control" id="challenge-question" placeholder="Question" form="question-form" required>
										</div>
									
										<div class="text-start">
											<p>Answers 
												<span class="text-secondary">[Choose the right one]</span>
											</p>
										</div>
									
										<div class="row">
											<div class="col-lg-6">
												<div class="input-group input-group-sm mb-2">
													<div class="input-group-text">
														<input class="form-check-input mt-0" type="radio" name="answer" id="radio0" value="0" form="question-form" required>
													</div>
													<input type="text" class="form-control" id="answer1" placeholder="Answer 1" form="question-form" name="0" required>
												</div>
											</div>
											<div class="col-lg-6">
												<div class="input-group input-group-sm mb-2">
													<div class="input-group-text">
														<input class="form-check-input mt-0" type="radio" name="answer" id="radio1" value="1" form="question-form">
													</div>
													<input type="text" class="form-control" id="answer2" placeholder="Answer 2" form="question-form" name="1" required>
												</div>
											</div>
											<div class="col-lg-6">
												<div class="input-group input-group-sm mb-2">
													<div class="input-group-text">
														<input class="form-check-input mt-0" type="radio" name="answer" id="radio2" value="2" form="question-form">
													</div>
													<input type="text" class="form-control" id="answer3" placeholder="Answer 3" form="question-form" name="2" required>
												</div>
											</div>
											<div class="col-lg-6">
												<div class="input-group input-group-sm mb-2">
													<div class="input-group-text">
														<input class="form-check-input mt-0" type="radio" name="answer" id="radio3" value="3" form="question-form">
													</div>
													<input type="text" class="form-control" id="answer4" placeholder="Answer 4" form="question-form" name="3" required>
												</div>
											</div>
										</div>

										<button id="questionBtn" form="question-form" class="mt-2 p-0 border-0">
											<input class="btn btn-green m-0" type="submit" value="Save question" form="question-form"/>
										</button>
									  
									</div>
								</div>

								<hr>

								<button id="createHuntBtn" form="create-form" class="mt-2 p-0 border-0">
									<input class="btn btn-green m-0" type="submit" value="Create Hunt" form="create-form"/>
								</button>            
								
							</div>
						</div>
						
					</main>							
				</div>
			</div>
		</div>
		
		<div aria-live="polite" aria-atomic="true" class="position-relative">
			<div class="toast-container fixed-bottom p-3" id="toast-container-id" style="z-index: 100">
			
			</div>
		</div>
		
		<script async
			src="https://maps.googleapis.com/maps/api/js?key=apiKey&callback=initMapCreate">
		</script>