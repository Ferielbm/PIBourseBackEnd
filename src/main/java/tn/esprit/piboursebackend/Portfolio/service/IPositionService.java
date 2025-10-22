package tn.esprit.piboursebackend.Portfolio.service;

import tn.esprit.piboursebackend.Portfolio.Entity.Position;

import java.util.List;

public interface IPositionService {
    Position createPosition(Position Position);
    Position getPositionById(Long id);
    List<Position> getAllPositions();
    Position updatePosition(Long id, Position Position);
    void deletePosition(Long id);
}
