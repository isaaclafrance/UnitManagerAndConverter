package com.isaacapps.unitconverterapp.models.unitmanager.datamodels;

import com.isaacapps.unitconverterapp.models.unitmanager.datamodels.repositories.IDualKeyNCategoryRepository;

public class BaseDataModel<T, U, V> {
    protected IDualKeyNCategoryRepository<T,U,V> repositoryWithDualKeyNCategory;

    public BaseDataModel(){}
    public BaseDataModel(IDualKeyNCategoryRepository<T,U,V> repositoryWithDualKeyNCategory){
        this.repositoryWithDualKeyNCategory = repositoryWithDualKeyNCategory;
    }

    public boolean combineWith(BaseDataModel<T, U, V> otherDataModel){
        return this.repositoryWithDualKeyNCategory.combineWith(otherDataModel.repositoryWithDualKeyNCategory);
    }

    public void setRepositoryWithDualKeyNCategory(IDualKeyNCategoryRepository<T,U,V> repositoryWithDualKeyNCategory){
        this.repositoryWithDualKeyNCategory = repositoryWithDualKeyNCategory;
    }

}
