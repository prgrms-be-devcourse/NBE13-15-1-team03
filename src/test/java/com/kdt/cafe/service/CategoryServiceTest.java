package com.kdt.cafe.service;

import com.kdt.cafe.dto.CategoryRequestDTO;
import com.kdt.cafe.dto.CategoryResponseDTO;
import com.kdt.cafe.entity.Category;
import com.kdt.cafe.exception.BusinessLogicException;
import com.kdt.cafe.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CategoryServiceTest {

    private CategoryService categoryService;
    private CategoryRepository categoryRepository;

    //셋업 매서드
    @BeforeEach //각 테스트마다 사전작업.
    void setUp() {

        categoryRepository = mock(CategoryRepository.class);
        categoryService = new CategoryService(categoryRepository);
    }

    @Test
    void findAll(){
        //사전 설정
        when(categoryRepository.findAll(any(Sort.class)))
                .thenReturn(List.of(new Category("커피"),new Category("디카페인")));
        //실제 실행
        List<CategoryResponseDTO> result = categoryService.findAll();
        //개수 확인
        assertEquals(2,result.size());
    }

    @Test
    void findById() { //정상 아이디 1
        //사전설정
        when(categoryRepository.findById(1))
                . thenReturn(Optional.of(new Category("커피")));
        //실제 실행
        CategoryResponseDTO result = categoryService.findById(1);
        //결과가 맞는지 확인
        assertEquals("커피",result.name());
    }

    @Test
    void findByWrongId() { //비정상 아이디 0
        //DB를 가지 않음. when then 필요없음
        //실행과 결과 확인
        assertThrows(BusinessLogicException.class, () -> categoryService.findById(0));
    }


    @Test
    void findByMissingId() { //없는 아이디 999
        //DB를 가지 않음. when then 필요없음
        //실행과 결과 확인
        assertThrows(BusinessLogicException.class, () -> categoryService.findById(999));
    }

    @Test
    void save() { //정상 등록
        //사전설정
        when(categoryRepository.save(any(Category.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        //실제 실행
        CategoryResponseDTO result = categoryService.save(new CategoryRequestDTO(("커피")));
        //결과가 맞는지 확인
        assertEquals("커피",result.name());
    }

    @Test
    void update() {
    }

    @Test
    void delete() {
    }
}