package co.freea.tsn.Training.Core.Service;


import co.freea.tsn.Training.Core.Models.PageResponse;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IService<T> {

    PageResponse<T> findAll(int page_number, int page_size);

    Optional<T> findById(UUID id);

    long count();

    void delete(T obj);

    void deleteById(UUID id);

    List<T> findAllById(Iterable<UUID> iterable);

    T insert_update(T obj);

}
