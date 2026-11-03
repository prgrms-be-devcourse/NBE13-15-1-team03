package com.kdt.cafe.service;

import com.kdt.cafe.dto.CategoryRequestDTO;
import com.kdt.cafe.dto.CategoryResponseDTO;
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
    @Transactional(readOnly = true)
    public List<CategoryResponseDTO> findAll(){
        return categoryRepository.findAll(Sort.by("id"))
                .stream().map(CategoryResponseDTO::from).toList();
    }

    //내부 단건 조회 - 엔티티 반환
    private Category getCategoryById(int id){
        if (id < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"id is negative");
        }
        return categoryRepository.findById(id).orElseThrow(() -> new NotfoundException("missing"));
    }

    //단건 조회
    @Transactional(readOnly = true)
    public CategoryResponseDTO findById(int id){
        return CategoryResponseDTO.from(getCategoryById(id));
    }

    //등록
    @Transactional
    public CategoryResponseDTO save(CategoryRequestDTO categoryRequestDTO) {
        String checkedName = nameCheck(categoryRequestDTO.name());
        nameAlreadyExist(checkedName,0);
        Category category = new Category(checkedName);
        return CategoryResponseDTO.from(categoryRepository.save(category));
    }

    //수정
    @Transactional
    public CategoryResponseDTO update(int id, CategoryRequestDTO categoryRequestDTO) {
        Category category = getCategoryById(id);
        String checkedName = nameCheck(categoryRequestDTO.name());
        nameAlreadyExist(checkedName,id);
        category.setName(checkedName);
        return CategoryResponseDTO.from(category);
    }

    //카테고리명 양식 검사 (글자수, null, 공백)
    private String nameCheck(String name){
        if(name == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name is null");
        }
        String onlyName = name.strip();
        if (onlyName.length()>30||onlyName.isEmpty()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Name is too long or empty");
        }
        return onlyName;
    }

    //카테고리명 중복 검사
    private void nameAlreadyExist(String name, int id){
        if (categoryRepository.existsByNameAndIdNot(name,id)){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Name is already exists");
        }
    }

//    //삭제
//    public Category delete(Category category){
//        categoryRepository.delete(category);
//        return category;
//    }

    //카테고리 별 메뉴 조회
//    public
}
