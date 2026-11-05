package com.kdt.cafe.service;

import com.kdt.cafe.dto.category.CategoryRequestDTO;
import com.kdt.cafe.dto.category.CategoryResponseDTO;
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
import static org.mockito.Mockito.*;

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
    void findAll(){ //전체 조회
        //사전 설정
        when(categoryRepository.findAll(any(Sort.class)))
                .thenReturn(List.of(new Category("커피"),new Category("디카페인")));
        //실제 실행
        List<CategoryResponseDTO> result = categoryService.findAll();
        //개수 확인
        assertEquals(2,result.size());
    }

    @Test
    void findById() { //단건 조회 정상 아이디 1
        //사전설정
        when(categoryRepository.findById(1))
                . thenReturn(Optional.of(new Category("커피")));
        //실제 실행
        CategoryResponseDTO result = categoryService.findById(1);
        //결과가 맞는지 확인
        assertEquals("커피",result.name());
    }

    @Test
    void findByWrongId() { //단건 조회 비정상 아이디 0
        //DB를 가지 않음. when then 필요없음
        //실행과 결과 확인
        assertThrows(BusinessLogicException.class, () -> categoryService.findById(0));
    }


    @Test
    void findByMissingId() { //단건 조회 없는 아이디 999
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
    void blankSave() { //DTO 공백 제거 테스트
        //사전설정
        when(categoryRepository.save(any(Category.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        //실제 실행
        CategoryResponseDTO result = categoryService.save(new CategoryRequestDTO((" 커피 ")));
        //결과가 맞는지 확인
        assertEquals("커피",result.name());
    }

    @Test
    void doubleSave() { //중복 카테고리명 등록 테스트
        //사전설정
        when(categoryRepository.existsByNameAndIdNot("커피",0))
                .thenReturn(true);
        //실제실행과 결과
        assertThrows(BusinessLogicException.class, () -> categoryService.save(new CategoryRequestDTO("커피")));
    }


    @Test
    void update() { //정상 수정
        when(categoryRepository.findById(1))
                .thenReturn(Optional.of(new Category("커피")));
        when(categoryRepository.existsByNameAndIdNot("라떼",1))
                .thenReturn(false);
        //실제 실행
        CategoryResponseDTO result =categoryService.update(1,new CategoryRequestDTO("라떼"));
        //결과
        assertEquals("라떼",result.name());
    }

    @Test
    void wrongIdUpdate() { //없는 아이디 업데이트
        //실행 및 에러 확인
        assertThrows(BusinessLogicException.class, () -> categoryService.update(999,new CategoryRequestDTO("라떼")));
    }

    @Test
    void doubleUpdate() { //중복 카테고리명 업데이트
        //사전설정
        when(categoryRepository.findById(3))
                .thenReturn(Optional.of(new Category("커피")));
        when(categoryRepository.existsByNameAndIdNot("라떼", 3))
                .thenReturn(true);
        //실행 및 실패 확인
        assertThrows(BusinessLogicException.class, () -> categoryService.update(3, new CategoryRequestDTO("라떼")));
    }

    @Test
    void sameUpdate() { //수정 이름이 이전과 같아도 되는지
        when(categoryRepository.findById(1))
                .thenReturn(Optional.of(new Category("커피")));
        when(categoryRepository.existsByNameAndIdNot("커피",1))
                .thenReturn(false);
        //실제 실행
        CategoryResponseDTO result =categoryService.update(1,new CategoryRequestDTO("커피"));
        //결과
        assertEquals("커피",result.name());
    }

    @Test
    void delete() { //정상 삭제
        //사전 준비
        when(categoryRepository.existsById(1))
                .thenReturn(true);
        //실행
        categoryService.delete(1);
        //결과
        verify(categoryRepository).deleteById(1);
    }

    @Test
    void wrongIdDelete() {
        assertThrows(BusinessLogicException.class, () -> categoryService.delete(999));
    }
}
