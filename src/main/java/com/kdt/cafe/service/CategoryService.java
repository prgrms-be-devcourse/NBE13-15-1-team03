package com.kdt.cafe.service;

import com.kdt.cafe.dto.CategoryRequestDTO;
import com.kdt.cafe.entity.Category;
import com.kdt.cafe.exception.NotfoundException;
import com.kdt.cafe.repository.CategoryRepository;
import org.apache.coyote.BadRequestException;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }
    //필요 한 매서드 종류

    //전체 조회
    @Transactional
    public List<Category> findAll(){
        return categoryRepository.findAll(Sort.by("id"));
    }

    //단건 조회
    @Transactional
    public Category findById(int id){
        if (id < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"id is negative");
        }
        return categoryRepository.findById(id).orElseThrow(() -> new NotfoundException("missing"));
    }

    //등록
    @Transactional
    public Category save(CategoryRequestDTO categoryRequestDTO) {
        Category category = new Category(categoryRequestDTO.name());
        return categoryRepository.save(category);
    }

    //수정
    @Transactional
    public Category update(int id, CategoryRequestDTO categoryRequestDTO) {
        Category category = findById(id);
        category.setName(categoryRequestDTO.name());
        return category;
    }

//    //삭제
//    public Category delete(Category category){
//        categoryRepository.delete(category);
//        return category;
//    }

    //카테고리 별 메뉴 조회
//    public
}
