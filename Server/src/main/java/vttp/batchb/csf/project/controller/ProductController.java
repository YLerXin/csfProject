package vttp.batchb.csf.project.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import vttp.batchb.csf.project.models.Product;
import vttp.batchb.csf.project.service.ProductService;
import vttp.batchb.csf.project.service.S3Service;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    @Autowired
    private ProductService productSvc;
    @Autowired 
    private S3Service s3Service;
    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping
    public ResponseEntity<Product> createNewProduct(@RequestBody Product product) {
        Product created = productSvc.createProduct(product);

        return ResponseEntity.ok(created);
    }

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> all = productSvc.findAll();
        return ResponseEntity.ok(all);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable String productId, @RequestBody Product p) {
        p.setProductId(productId);
        Product updated = productSvc.updateProduct(p);
        return ResponseEntity.ok(updated);
    } 
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable String id) {
        productSvc.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProducts(@RequestParam String q) {
        List<Product> results = productSvc.searchByNameOrTags(q);
        return ResponseEntity.ok(results);
    }

    @PostMapping("/{productId}/images")
    public ResponseEntity<?> uploadProductImage(
    @PathVariable String productId,
    @RequestParam("file") MultipartFile file
) {
    try {
        String imageUrl = s3Service.uploadProductImage(file, productId);

        Product p = productSvc.findById(productId);
        if (p == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                 .body(Map.of("message", "Product not found"));
        }
        p.getImages().add(imageUrl);
        Product updated = productSvc.updateProduct(p);

        return ResponseEntity.ok(Map.of(
            "message", "Image uploaded",
            "imageUrl", imageUrl,
            "product", updated
        ));
    } catch (IOException e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                             .body(Map.of("message", "Error uploading product image"));
    }
}

@PostMapping("/multipart")
public ResponseEntity<?> createProductWithImages(
    @RequestParam("userId") String userId,
    @RequestParam("productName") String productName,
    @RequestParam("productDetails") String productDetails,
    @RequestParam("condition") String condition,
    @RequestParam("price") BigDecimal price,
    @RequestParam("tags") String tagsJson,
    @RequestParam(value="files",required=false) List<MultipartFile> files
) {
    List<String> tags = new ArrayList<>();
    try {
        tags = objectMapper.readValue(tagsJson, new TypeReference<List<String>>() {});
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(Map.of("message", "Invalid tags JSON"));
    }
    List<String> uploadedUrls = new ArrayList<>();
    if (files == null || files.isEmpty()) {
        String placeholderUrl = "https://csfprojectlx.sgp1.cdn.digitaloceanspaces.com/placeholder-images-image_large.webp";
        uploadedUrls.add(placeholderUrl);
    }else{
    for (MultipartFile file : files) {
        try {
            String imageUrl = s3Service.uploadProductImage(file, "temp-id-or-uuid"); 
            uploadedUrls.add(imageUrl);
        } catch (IOException ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Failed to upload one of the images"));
        }
    }
}
    Product product = new Product();
    product.setUserId(userId);
    product.setProductName(productName);
    product.setProductDetails(productDetails);
    product.setCondition(condition);
    product.setPrice(price);
    product.setAvailability(true);
    product.setTags(tags);
    product.setImages(uploadedUrls);

    Product created = productSvc.createProduct(product);

    return ResponseEntity.ok(created);
}

@GetMapping("/{productId}")
public ResponseEntity<Product> getByProductId(@PathVariable String productId) {
    Product found = productSvc.findByProductId(productId);
    if (found == null) {
        return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(found);
}
@GetMapping("/searchAvail")
public ResponseEntity<List<Product>> searchAvailable(@RequestParam String q) {
    List<Product> results = productSvc.searchAvailable(q);
    return ResponseEntity.ok(results);
}
}

