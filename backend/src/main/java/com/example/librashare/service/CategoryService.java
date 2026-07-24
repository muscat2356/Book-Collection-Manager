package com.example.librashare.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.librashare.domain.CategoryLarge;
import com.example.librashare.domain.CategoryMedium;
import com.example.librashare.domain.CategorySmall;
import com.example.librashare.dto.response.CategoryNodeResponse;
import com.example.librashare.repository.CategoryLargeRepository;
import com.example.librashare.repository.CategoryMediumRepository;
import com.example.librashare.repository.CategorySmallRepository;

/**
 * カテゴリーをDBから取得しレスポンスへ変換するサービスクラス
 * @author ichikura
 * CategoryService
 */
@Service
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryLargeRepository largeRepository;
    private final CategoryMediumRepository mediumRepository;
    private final CategorySmallRepository smallRepository;

    public CategoryService(CategoryLargeRepository largeRepository, CategoryMediumRepository mediumRepository,
            CategorySmallRepository smallRepository) {
        this.largeRepository = largeRepository;
        this.mediumRepository = mediumRepository;
        this.smallRepository = smallRepository;
    }

    /**
     * カテゴリーを取得し、レスポンスへ変換するメソッド
     * @return レスポンスDTO
     */
    public List<CategoryNodeResponse> getTree() {
        // カテゴリーをDBから取得
        List<CategoryLarge> larges = largeRepository.findAllByOrderBySortOrderAscIdAsc();
        List<CategoryMedium> mediums = mediumRepository.findAllByOrderBySortOrderAscIdAsc();
        List<CategorySmall> smalls = smallRepository.findAllByOrderBySortOrderAscIdAsc();

        // 中カテゴリーを大カテゴリーidでグルーピング
        Map<Long, List<CategoryMedium>> mediumByLargeId = groupMediumsByLargeId(mediums);
        // 小カテゴリーを中カテゴリーidでグルーピング
        Map<Long, List<CategorySmall>> smallsByMediumId = groupSmallsByMediumId(smalls);

        // 該当idに合わせて変換・組み立て
        return larges.stream()
                .map(large -> toLargeNode(large, mediumByLargeId, smallsByMediumId))
                .toList();
        
    }

    /**
     * 中カテゴリーを大カテゴリーidでグルーピング
     * @param mediums 中カテゴリーリスト
     * @return 大カテゴリーidをkey・該当中カテゴリーリストをValueとしたMap
     */
    private Map<Long, List<CategoryMedium>> groupMediumsByLargeId(List<CategoryMedium> mediums) {
        return mediums.stream()
                .collect(Collectors.groupingBy(m -> m.getCategoryLarge().getId()));
    }

    /**
     * 小カテゴリーを中カテゴリーidでグルーピング
     * @param smalls 小カテゴリーリスト
     * @return 中カテゴリーidをkey・該当小カテゴリーリストをValueとしたMap
     */
    private Map<Long, List<CategorySmall>> groupSmallsByMediumId(List<CategorySmall> smalls) {
        return smalls.stream()
                .collect(Collectors.groupingBy(s -> s.getCategoryMedium().getId()));
    }
    
    /**
     * 大カテゴリーをレスポンスへ変換するメソッド
     * @param large 大カテゴリ1件
     * @param mediumByLargeId 大カテゴリーidをkey・該当中カテゴリーリストをValueとしたMap
     * @param smallsByMediumId 中カテゴリーidをkey・該当小カテゴリーリストをValueとしたMap
     * @return 大カテゴリーレスポンスDTO
     */
    private CategoryNodeResponse toLargeNode(
        CategoryLarge large,
        Map<Long, List<CategoryMedium>> mediumByLargeId,
        Map<Long, List<CategorySmall>> smallsByMediumId
    ) {
        CategoryNodeResponse node = new CategoryNodeResponse(large.getId(), large.getName());
        // 大カテゴリーidをkeyで該当中カテゴリーを取り出し、レスポンスへ変換
        List<CategoryNodeResponse> children = 
                                mediumByLargeId.getOrDefault(large.getId(), List.of())
                                .stream()
                                .map(m -> toMediumNode(m, smallsByMediumId))
                                .toList();
        node.setChildren(children);
        return node;
    }

    /**
     * 中カテゴリーをレスポンスへ変換するメソッド
     * @param medium 中カテゴリ1件
     * @param smallsByMediumId 中カテゴリーidをkey・該当小カテゴリーリストをValueとしたMap
     * @return 中カテゴリーレスポンスDTO
     */
    private CategoryNodeResponse toMediumNode(
        CategoryMedium medium,
        Map<Long, List<CategorySmall>> smallsByMediumId
    ) {
        CategoryNodeResponse node = new CategoryNodeResponse(medium.getId(), medium.getName());
        // 中カテゴリーidをkeyで該当小カテゴリーを取り出し、レスポンスへ変換
        List<CategoryNodeResponse> children = smallsByMediumId.getOrDefault(medium.getId(), List.of())
                                                        .stream()
                                                        .map(c -> toSmallNode(c))
                                                        .toList();
        node.setChildren(children);
        return node;
    }

    /**
     * 小カテゴリーをレスポンスDTOへ変換するメソッド
     * @param small 小カテゴリ1件
     * @return 小カテゴリーレスポンスDTO
     */
    private CategoryNodeResponse toSmallNode(CategorySmall small){
        return new CategoryNodeResponse(small.getId(), small.getName());
    }
    

}
