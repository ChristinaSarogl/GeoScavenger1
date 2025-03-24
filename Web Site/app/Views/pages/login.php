    <div class="container pt-5">
        <div class="row d-flex justify-content-center align-items-center">
            <div class="col-12 col-md-8 col-lg-6 col-xl-5">
                <div class="card" style="border-radius: 1rem; background: white;">
                    <div class="card-body px-5 py-2 text-center">
                        <p class="fw-bold fs-4 mb-3 text-uppercase title">Login</p>
						
                        <p class="mb-3" id="login-error-message">Please enter your login and password</p>

                        <form class="text-start" id="login-form">							
							<div class="form-floating mb-3 mx-3">
								<input type="email" class="form-control" id="login-email" placeholder="name@example.com" required>
								<label for="login-email">Email address</label>
							</div>
							
							<div class="form-floating mb-3 mx-3">
								<input type="password" class="form-control" id="login-password" placeholder="Password" required>
								<label for="login-password">Password</label>
							</div>

                            <p class="small mb-3 px-3 pb-lg-2 text-end"><a class="text-secondary"
								href="<?php echo base_url(); ?>/reset-password">Forgot password?</a></p>

                            <div class="mt-4 text-center">
								<input class="btn btn-green w-50" type="submit" value="Login"/>
                            </div>
                            
                        </form>

                        <div>
                            <p class="mt-5 mb-2">Don't have an account?
                                <a href="<?php echo base_url(); ?>/register" class="text-success">Register</a>
                            </p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>