package com.exercise.school.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@EqualsAndHashCode
public class PagedResponse<T> {
	private final List<T> content;
	private final PaginationDto pagination;

	public PagedResponse(Page<T> queryResult) {
		this.content = queryResult.getContent();
		this.pagination = new PaginationDto(
				queryResult.getPageable().getOffset(),
				queryResult.getNumber(),
				queryResult.getSize(),
				queryResult.isLast(),
				queryResult.getTotalElements()
		);
	}
}