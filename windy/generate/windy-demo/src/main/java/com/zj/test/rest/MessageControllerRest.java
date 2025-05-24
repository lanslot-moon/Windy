package com.zj.test.rest;

import com.zj.test.model.*;
import com.zj.test.service.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@RestController
public class MessageControllerRest {

  private final IMessageControllerService MessageControllerService;

  public MessageControllerRest(IMessageControllerService MessageControllerService){
    this.MessageControllerService = MessageControllerService;
  }

  @RequestMapping(value = "/message", method = RequestMethod.GET)
  public ResponeModel showMessage() {
    return MessageControllerService.showMessage();
  }

}
