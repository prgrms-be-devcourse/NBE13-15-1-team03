package com.kdt.cafe.service;

import com.kdt.cafe.dto.category.CategoryRequestDTO;
import com.kdt.cafe.dto.category.CategoryResponseDTO;
import com.kdt.cafe.entity.Category;
import com.kdt.cafe.exception.BusinessLogicException;
import com.kdt.cafe.exception.ErrorCode;
import com.kdt.cafe.repository.CategoryRepository;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        validateId(id);
        return categoryRepository.findById(id).orElseThrow(() -> new BusinessLogicException(ErrorCode.CATEGORY_NOT_FOUND)); //해당 카테고리 찾을 수 없음
    }

    //Id 잘못된 id 확인(0이하)
    private void validateId(int id){
        if (id < 1) {throw new BusinessLogicException(ErrorCode.VALIDATION_ERROR,"categoryId","잘못된 아이디입니다");
        }
    }

    //단건 조회
    @Transactional(readOnly = true)
    public CategoryResponseDTO findById(int id){
        return CategoryResponseDTO.from(getCategoryById(id));
    }

    //등록
    @Transactional
    public CategoryResponseDTO save(CategoryRequestDTO categoryRequestDTO) {
        nameAlreadyExist(categoryRequestDTO.name(),0);
        Category category = new Category(categoryRequestDTO.name());
        return CategoryResponseDTO.from(categoryRepository.save(category));
    }

    //수정
    @Transactional
    public CategoryResponseDTO update(int id, CategoryRequestDTO categoryRequestDTO) {
        Category category = getCategoryById(id);
        nameAlreadyExist(categoryRequestDTO.name(),id);
        category.setName(categoryRequestDTO.name());
        return CategoryResponseDTO.from(category);
    }

    //카테고리명 중복 검사
    private void nameAlreadyExist(String name, int id){
        if (categoryRepository.existsByNameAndIdNot(name,id)){
            throw new BusinessLogicException(ErrorCode.CATEGORY_NAME_DUPLICATED);
        }
    }

    //삭제
    @Transactional
    public void delete(int id){
        validateId(id);
        if (!categoryRepository.existsById(id)){
            throw new BusinessLogicException(ErrorCode.CATEGORY_NOT_FOUND);
        }
        categoryRepository.deleteById(id);
    }

    //카테고리 별 메뉴 조회
//    public
}
