package vttp.batchb.csf.project.models;

import java.util.List;

public class UpdateItemRequest {
        private List<Product> initiatorItems;
        private List<Product> ownerItems;

        public List<Product> getInitiatorItems() {
            return initiatorItems;
        }
        public void setInitiatorItems(List<Product> initiatorItems) {
            this.initiatorItems = initiatorItems;
        }

        public List<Product> getOwnerItems() {
            return ownerItems;
        }
        public void setOwnerItems(List<Product> ownerItems) {
            this.ownerItems = ownerItems;
        }
}
