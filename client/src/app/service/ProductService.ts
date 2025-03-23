import { HttpClient } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { Observable } from "rxjs";
import { Product } from "../model/Products";

@Injectable()
export class ProductService{
private http=inject(HttpClient)

createProduct(product:Product):Observable<any>{
    return this.http.post('/api/products', product);
}
getAllProducts(): Observable<Product[]> {
return this.http.get<Product[]>('/api/products');
}
getProductById(productId: string): Observable<Product> {
return this.http.get<Product>(`/api/products/${productId}`);
}
updateProduct(id: string, product: Product): Observable<Product> {
return this.http.put<Product>(`/api/products/${id}`, product);
}
deleteProduct(id: string): Observable<void> {
return this.http.delete<void>(`/api/products/${id}`);
}
searchProducts(keyword: string): Observable<Product[]> {
return this.http.get<Product[]>(`/api/products/search`, {
    params: { q: keyword }
});
}
uploadProductImage(productId: string, file: File): Observable<any> {
const formData = new FormData();
formData.append('file', file); 
return this.http.post(`/api/products/${productId}/images`, formData);
}
createProductWithImages(formData: FormData): Observable<any> {
return this.http.post('/api/products/multipart', formData);
}

searchAvailable(keyword: string): Observable<Product[]> {
    return this.http.get<Product[]>('/api/products/searchAvail', {
      params: { q: keyword }
    });
  }
}