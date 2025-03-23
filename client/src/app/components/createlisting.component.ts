import { Component, inject, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { ProductService } from '../service/ProductService';
import { Router } from '@angular/router';
import { Product } from '../model/Products';
import { Store } from '@ngrx/store';
import { selectUserId } from '../utils/auth.selectors';
import { take } from 'rxjs';
import { MatChipInputEvent } from '@angular/material/chips';
import { NgbCarouselConfig, NgbCarouselModule } from '@ng-bootstrap/ng-bootstrap';


@Component({
  selector: 'app-createlisting',
  standalone: false,
  templateUrl: './createlisting.component.html',
  styleUrl: './createlisting.component.css',
  providers: [NgbCarouselConfig]
})
export class CreatelistingComponent implements OnInit {

  listingForm!: FormGroup;

  private fb = inject(FormBuilder);
  private productSvc = inject(ProductService);
  private router = inject(Router);
  private store = inject(Store);

  tagsControl = new FormControl<string[]>([], { nonNullable: true });
  imagesPreview: string[] = [];
  tempFiles: File[] = [];

  
  selectedFile?: File;

  ngOnInit(): void {
    this.listingForm = this.fb.group({
      productName: ['', Validators.required],
      productDetails: [''],
      condition: ['Used'], 
      price: [0, [Validators.min(0)]],
    });

  }
  onFileChange(event: any) {
    const fileList: FileList = event.target.files;
    if (!fileList) return;
  
    for (let i = 0; i < fileList.length; i++) {
      const file = fileList[i];
      this.tempFiles.push(file);
      const reader = new FileReader();
      reader.onload = () => {
        const dataUrl = reader.result as string;
        this.imagesPreview.push(dataUrl);
      };
      reader.readAsDataURL(file);
    }
  }

  addTag(event: MatChipInputEvent): void {
    const value = (event.value || '').trim();
    if (value) {
      const currentTags = this.tagsControl.value;
      this.tagsControl.setValue([...currentTags, value]);
    }
    event.chipInput!.clear();
  }
  removeTag(tag: string): void {
    const updated = this.tagsControl.value.filter(t => t !== tag);
    this.tagsControl.setValue(updated);
  }
  onConditionToggle(checked: boolean): void {
    this.listingForm.patchValue({ condition: checked ? 'New' : 'Used' });
  }
  onSubmit() {
    if (!this.listingForm.valid) return;
    this.store.select(selectUserId).pipe(take(1)).subscribe(uid => {
      if (!uid) {
        console.error("No user ID found in store!");
        return;
      }
      const formData = new FormData();
      formData.append("userId", uid);
      formData.append("productName", this.listingForm.value.productName);
      formData.append("productDetails", this.listingForm.value.productDetails);
      formData.append("condition", this.listingForm.value.condition);
      formData.append("price", this.listingForm.value.price.toString());
      formData.append("tags", JSON.stringify(this.tagsControl.value));
      for (const file of this.tempFiles) {
        formData.append("files", file);
      }
      this.productSvc.createProductWithImages(formData).subscribe({
        next: (res) => {
          console.log("Product created with images:", res);
          this.router.navigate(["/profile", uid]);
        },
        error: (err) => console.error("Error creating product with images", err)
      });
    });
  }

  
}