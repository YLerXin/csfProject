package vttp.batchb.csf.project.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import vttp.batchb.csf.project.models.Product;
import vttp.batchb.csf.project.repository.ProductRepository;

@Service
public class ProductService {
    @Autowired
    private ProductRepository productRepo;

    public Product createProduct(Product p) {
        return productRepo.createProduct(p);
    }

    public List<Product> findAll() {
        return productRepo.findAll();
    }

    public Product findById(String id) {
        return productRepo.findById(id);
    }

    public Product updateProduct(Product p) {
        return productRepo.updateProduct(p);
    }

    public void deleteProduct(String id) {
        productRepo.deleteProduct(id);
    }

    public List<Product> searchByNameOrTags(String keyword) {
        return productRepo.searchByNameOrTags(keyword);
    }
    
    public Product findByProductId(String productId){
        return productRepo.findByProductId(productId);
    }

    public List<Product> searchAvailable(String keyword) {
        return productRepo.searchByNameOrTagsAndAvailability(keyword);
    }

    public void setAvailability(String productId, boolean isAvailable) {
        productRepo.setAvailabilityByProductId(productId, isAvailable);
    }
}
