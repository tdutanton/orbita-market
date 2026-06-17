package paymentsService.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import paymentsService.service.CentralService;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentsServiceController {

  private final CentralService centralService;

  @PostMapping("/users")
  public ResponseEntity<Long> addUser() {
    Long id = centralService.createAndSaveUser();
    return ResponseEntity.ok(id);
  }
}
