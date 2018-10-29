package com.isaacapps.unitconverterapp.models.unitmanager.datamodels;

import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.repositories.IDualKeyNCategoryRepository;

public class BaseDataModel<T, U, V> {
    protected IDualKeyNCategoryRepository<T,U,V> repositoryWithDualKeyNCategory;

    public BaseDataModel(){}
    public BaseDataModel(IDualKeyNCategoryRepository<T,U,V> repositoryWithDualKeyNCategory){
        this.repositoryWithDualKeyNCategory = repositoryWithDualKeyNCategory;
    }

    public boolean combineWith(BaseDataModel<T, U, V> otherDataModel){
        if(otherDataModel == null)
            return true;

        if(this.repositoryWithDualKeyNCategory == null){
            this.repositoryWithDualKeyNCategory = otherDataModel.repositoryWithDualKeyNCategory;
            return true;
        }

        return this.repositoryWithDualKeyNCategory.combineWith(otherDataModel.repositoryWithDualKeyNCategory);
    }

    public void setRepositoryWithDualKeyNCategory(IDualKeyNCategoryRepository<T,U,V> repositoryWithDualKeyNCategory){
        this.repositoryWithDualKeyNCategory = repositoryWithDualKeyNCategory;
    }

}
