	<div class="container pt-5">
        <div class="row d-flex justify-content-center align-items-center">
            <div class="col-12 col-md-8 col-lg-6 col-xl-5">
                <div class="card" style="border-radius: 1rem; background: white;">
                    <div class="card-body px-5 py-2 text-center">
                        <p class="fw-bold fs-4 mb-1 text-uppercase title">Reset Password</p>					
						<small class="text-secondary">Enter your email address, and we'll send you a link to get back into your account.</small>
						
						<p class="mt-2 text-danger" id="reset-error-message" style="display: none">Please enter a valid email.</p>
						
                        <form class="text-start mt-4" id="reset-form">
                            <div class="mb-3 px-3">
                                <input type="email" class="form-control" id="reset-email" placeholder="Email address" required> 
								
                            <div class="mt-3 text-center">
								<input class="btn btn-green m-0" type="submit" value="Send email"/>
                            </div>
                            
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>