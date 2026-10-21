package edu.srmist.outpass.api;

import edu.srmist.outpass.api.OutpassController.OutpassRequest;
import io.camunda.client.CamundaClient;
import io.camunda.client.api.response.ProcessInstanceEvent;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/outpass")
public class OutpassController {

    private final CamundaClient camundaClient;

    public OutpassController(CamundaClient camundaClient) {
        this.camundaClient = camundaClient;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> applyOutpass(@RequestBody OutpassRequest request) {
        ProcessInstanceEvent instance = camundaClient.newCreateInstanceCommand()
                .bpmnProcessId("hostel-outpass")
                .latestVersion()
                .variables(request)
                .send()
                .join();

        return ResponseEntity.ok(Map.of(
                "message", "Out-pass request submitted",
                "processInstanceKey", instance.getProcessInstanceKey()
        ));
    }

    public record OutpassRequest(
            String studentName,
            String regNo,
            String hostelBlock,
            String outDate,
            String returnDate,
            String parentPhone,
            String purpose
    ) {
    }
}
