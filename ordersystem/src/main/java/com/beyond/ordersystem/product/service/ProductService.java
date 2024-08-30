package com.beyond.ordersystem.product.service;

import com.beyond.ordersystem.common.service.StockInventoryService;
import com.beyond.ordersystem.product.domain.Product;
import com.beyond.ordersystem.product.dto.ProductResDto;
import com.beyond.ordersystem.product.dto.ProductSaveReqDto;
import com.beyond.ordersystem.product.dto.ProductSearchDto;
import com.beyond.ordersystem.product.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ProductService {
    private final ProductRepository productRepository;
    private final StockInventoryService stockInventoryService;
    private final S3Client s3Client;
    @Autowired
    public ProductService(ProductRepository productRepository, StockInventoryService stockInventoryService, S3Client s3Client) {
        this.productRepository = productRepository;
        this.stockInventoryService = stockInventoryService;
        this.s3Client = s3Client;
    }

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.s3.folder}")
    private String folder;

    public Product createProduct(ProductSaveReqDto dto) {
        MultipartFile image = dto.getProductImage();
        Product product = null;
        try {
            product = productRepository.save(dto.toEntity());
            byte[] bytes = image.getBytes();
            Path path = Paths.get("C:/Users/Playdata1/Desktop/temp", product.getId() + "_" + image.getOriginalFilename());
            Files.write(path, bytes, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            product.updateImagePath(path.toString());

            if(dto.getName().contains("sale")) {
                stockInventoryService.increaseStock(product.getId(), dto.getStockQuantity());
            }

        }catch (IOException e) {
            throw new RuntimeException("이미지 저장 실패");
        }
        return product;
    }

    public Page<ProductResDto> listProduct(ProductSearchDto searchDto, Pageable pageable) {
        // 검색을 위해 Specification 객체 사용
        // Specification 객체는 복잡한 쿼리를 명세를 이용하여 정의하는 방식으로, 쿼리를 쉽게 생성
        Specification<Product> specification = new Specification<Product>() {
            @Override
            public Predicate toPredicate(Root<Product> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                if(searchDto.getSearchName() != null) {
                    // root: 엔티티의 속성을 접근하기 위한 객체, CriteriaBuilder는 쿼리를 생성하기 위한 객체
                    predicates.add(criteriaBuilder.like(root.get("name"), "%"+searchDto.getSearchName()+"%"));
                }
                if(searchDto.getCategory() != null) {
                    predicates.add(criteriaBuilder.like(root.get("category"), "%"+searchDto.getCategory()+"%"));
                }
                Predicate[] predicateArr = new Predicate[predicates.size()];
                for(int i=0; i<predicateArr.length; i++) {
                    predicateArr[i] = predicates.get(i);
                }
                // 위 2개의 쿼리 조건문을 and조건으로 연결
                Predicate predicate = criteriaBuilder.and(predicateArr);
                return predicate;
            }
        };
        Page<Product> products = productRepository.findAll(specification, pageable);
        Page<ProductResDto> dtos = products.map(a->a.fromEntity());
        return dtos;
    }

    public Product createAWSProduct(ProductSaveReqDto saveReqDto){

        MultipartFile image = saveReqDto.getProductImage();
        Product product = null;

        try {
            product = productRepository.save(saveReqDto.toEntity());
            byte[] bytes = image.getBytes();
            String fileName = product.getId() + "_" + image.getOriginalFilename();
            Path path = Paths.get("C:/Users/Playdata1/Desktop/temp/", fileName );

//          local pc에 임시 저장.
            Files.write(path,bytes,StandardOpenOption.CREATE,StandardOpenOption.WRITE);//저 경로에 bytes(이미지파일)을 저장하겠다.
//          라이브러리를 사용하여 AWS에 pc에 저장된 파일을 업로드 한다.
            PutObjectRequest putObjectRequest = PutObjectRequest
                    .builder()
                    .bucket(bucket)
                    .key(fileName)
                    .build();

            // Bean 객체로 만든 S3 클라이언트를 가져와서 파일을 담아서 전송한다.
            PutObjectResponse putObjectResponse
                    = s3Client.putObject(putObjectRequest, RequestBody.fromFile(path));

            // S3에 올라가있는 경로를 담아서 저장한다. (S3에 가서 (경로를)가져온다 라고 봐도 무방할 것 같다.)
            String S3Path // a에서 값을 꺼내는 것. filename으로 찾아와라.  그럼 파일이 저장되어있는 s3 경로가 S3Path 변수에 저장된다.
                    = s3Client.utilities().getUrl(a -> a.bucket(bucket).key(fileName)).toExternalForm();
            product.updateImagePath(S3Path);

        } catch (IOException e) {
            //애매할떄는 런타임 exception 던진다, 예외를 터트려줘야 트랜잭션 예외 처리가 된다.
            throw new RuntimeException("이미지 저장에 실패했습니다.");
        }
        System.out.println("ok");

        return product;
    }
}
