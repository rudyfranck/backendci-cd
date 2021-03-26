package co.freea.tsn.Training.Core;

import co.freea.tsn.Training.Core.Models.Operation;
import co.freea.tsn.Training.Core.Models.PageResponse;
import co.freea.tsn.Training.Core.Models.ResponseModel.DeepMapper;
import co.freea.tsn.Training.Core.Service.UniversalRepository;
import org.davidmoten.rx.jdbc.Database;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
@RestController
@RequestMapping("/api/v0.1/operations")
@CrossOrigin(origins = "*")
public class OperationController {

    @Autowired
    private UniversalRepository<Operation> repo;

    @Autowired
    private Database database;

    @CrossOrigin(origins = "*")
    @PostMapping
    public Mono<Integer> insert(@RequestBody Operation operation) throws Exception {
        return repo.insert(operation);
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/search")
    public Mono<PageResponse<Map<String, Object>>> findSearch(@RequestParam int page_number, @RequestParam int page_size, @RequestBody(required = false) Operation operation) throws Exception {
        if (page_size <= 0) {
            throw new ArrayIndexOutOfBoundsException();
        }
        return repo
                .findDeepOne(operation, page_number, page_size)
                .map(page -> new PageResponse<>(
                                page.getPage_count(),
                                page.getData()
                                        .stream()
                                        .map(u -> new DeepMapper(database).toMap(u))
                                        .map(s -> s.orElse(new JSONObject()).toMap())
                                        .collect(Collectors.toList())
                        )
                );
    }

    @CrossOrigin(origins = "*")
    @GetMapping
    public Mono<PageResponse<Map<String, Object>>> findAll(@RequestParam int page_number, @RequestParam int page_size) throws Exception {
        if (page_size <= 0) {
            throw new ArrayIndexOutOfBoundsException();
        }
        return repo
                .findAll(Operation.class, page_number, page_size)
                .map(page -> new PageResponse<>(
                                page.getPage_count(),
                                page.getData()
                                        .stream()
                                        .map(u -> new DeepMapper(database).toMap(u))
                                        .map(s -> s.orElse(new JSONObject()).toMap())
                                        .collect(Collectors.toList())
                        )
                );
    }

    @CrossOrigin(origins = "*")
    @DeleteMapping("/{id}")
    public Mono<Integer> delete(@PathVariable("id") @RequestBody UUID id) throws Exception {
        return repo.deleteById(id, Operation.class);
    }

}
