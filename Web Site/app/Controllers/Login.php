<?php

namespace App\Controllers;

class Login extends BaseController
{
    public function login()
    {
        echo view('templates/entryHeader');
		echo view('pages/login');
		echo view('templates/entryFooter');
    }
	
	public function register()
	{
		echo view('templates/entryHeader');
		echo view('pages/register');
		echo view('templates/entryFooter');
	}
	
	public function resetPassword(){
		echo view('templates/entryHeader');
		echo view('pages/resetPassword');
		echo view('templates/entryFooter');
	}
}