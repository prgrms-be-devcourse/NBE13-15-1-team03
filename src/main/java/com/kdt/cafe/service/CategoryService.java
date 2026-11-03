package com.kdt.cafe.service;

import com.kdt.cafe.entity.Category;
import com.kdt.cafe.repository.CategoryRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }
    //필요 한 매서드 종류

    //전체 조회
    public List<Category> findAll(){
        return categoryRepository.findAll(Sort.by("id"));
    }

//    //단건 조회
//    public Category findById(int id){
//        return categoryRepository.findById(id);
//    }
//
//    //등록
//    public Category save(Category category){
//        categoryRepository.save(category);
//        return category;
//    }
//
//    //수정
//    public Category update(Category category){
//        categoryRepository.save(category);
//        return category;
//    }
//
//    //삭제
//    public Category delete(Category category){
//        categoryRepository.delete(category);
//        return category;
//    }

    //카테고리 별 메뉴 조회
//    public
}
