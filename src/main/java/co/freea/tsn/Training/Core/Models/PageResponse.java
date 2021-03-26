package co.freea.tsn.Training.Core.Models;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@ToString
@Setter
@Getter
@RequiredArgsConstructor
public class PageResponse<T> {
    private final long page_count;
    private final List<T> data;
}
