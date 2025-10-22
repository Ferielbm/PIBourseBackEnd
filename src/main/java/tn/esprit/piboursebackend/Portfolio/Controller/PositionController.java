package tn.esprit.piboursebackend.Portfolio.Controller;

import org.springframework.web.bind.annotation.*;
import tn.esprit.piboursebackend.Portfolio.Dto.PositionView;
import tn.esprit.piboursebackend.Portfolio.Entity.Position;
import tn.esprit.piboursebackend.Portfolio.service.PositionService;


import java.util.List;

@RestController
@RequestMapping("/positions")
public class PositionController {

    private final PositionService PositionService;

    public PositionController(PositionService PositionService) {
        this.PositionService = PositionService;
    }

    @GetMapping
    public List<PositionView> getAllPositions() {
        return PositionService.getAllPositions().stream().map(p -> {
            var s = p.getStock();
            s.getId();
            return new PositionView(
                    p.getPositionId(),
                    s.getId(),
                    s.getSymbol(),
                    p.getQuantity(),
                    p.getAveragePrice()
            );
        }).toList();
    }

    @PostMapping
    public Position createPosition(@RequestBody Position Position) {
        return PositionService.createPosition(Position);
    }

    @DeleteMapping("/{id}")
    public void deletePosition(@PathVariable Long id) {
        PositionService.deletePosition(id);
    }
}
