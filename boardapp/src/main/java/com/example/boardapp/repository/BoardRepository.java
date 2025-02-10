package com.example.boardapp.repository;

import com.example.boardapp.domain.Board;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BoardRepository extends CrudRepository<Board, Long>, PagingAndSortingRepository<Board, Long> {
}
