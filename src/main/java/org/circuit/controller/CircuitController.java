package org.circuit.controller;

import org.circuit.env.Environment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController()
@RequestMapping("/circuit")
public class CircuitController {
	
	@Autowired
	private Environment environment;
	
	@RequestMapping(method = RequestMethod.GET ,path = "/random")
    public @ResponseBody String getRandom() {
        return environment.randomPick();
    }

	@RequestMapping(method = RequestMethod.POST )
    public void put(@RequestBody String base64Circuit) {
        environment.insert(base64Circuit);
    }

}
