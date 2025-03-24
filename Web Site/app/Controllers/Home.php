<?php

namespace App\Controllers;

class Home extends BaseController
{
    public function home()
    {
        echo view('templates/homeHeader');
		echo view('pages/home');
		echo view('templates/homeFooter');
    }
	
	public function create(){
		echo view('templates/homeHeader');
		echo view('pages/create');
		echo view('templates/homeFooter');
	}
	
	public function active($huntID){
		$data['huntId'] = $huntID;
		
		echo view('templates/homeHeader');
		echo view('pages/activePlayers', $data);
		echo view('templates/homeFooter');
	}
}
