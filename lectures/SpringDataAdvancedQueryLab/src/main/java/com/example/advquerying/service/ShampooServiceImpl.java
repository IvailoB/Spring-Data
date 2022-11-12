package com.example.advquerying.service;

import com.example.advquerying.entities.Shampoo;
import com.example.advquerying.entities.Size;
import com.example.advquerying.repository.ShampooRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ShampooServiceImpl implements ShampooService{

    private final ShampooRepository shampooRepository;

    @Autowired
    public ShampooServiceImpl(ShampooRepository shampooRepository) {
        this.shampooRepository = shampooRepository;
    }

    @Override
    public List<Shampoo> selectBySize(Size size) {
       return this.shampooRepository.findBySize(size);
    }

    @Override
    public List<Shampoo> selectBySizeOrLabelId(Size size, long labelId) {

        return this.shampooRepository.findBySizeOrLabelIdOrderByPriceAsc(size,labelId);
    }

    @Override
    public List<Shampoo> selectMoreExpensiveThan(BigDecimal price) {
        return this.shampooRepository.findByPriceGreaterThanOrderByPriceDesc(price);
    }

    @Override
    public int countShampooByPrice(BigDecimal price) {
        return this.shampooRepository.countShampooByPriceLessThan(price);
    }

    @Override
    public List<Shampoo> selectShampooByIngredientsCount(int count) {
        return this.shampooRepository.findByIngredientCountLessThan(count);
    }
}
