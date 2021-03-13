package com.example.componentframework

import android.content.Intent

abstract class NerveCell: NervousSystemService(){
	// Move along route
	fun evaluateActionPotential(signal: Intent){

	}
}

// This handles comprehensive nervous system services
abstract class NervousSystem: NervousSystemService(){

}

// This handles medium complexity nervous processes
abstract class SpinalColumn: NervousSystemService(){

}

// This handles complex thought-processes, like ProcessorModule
abstract class Cortex: NervousSystemService(){

}