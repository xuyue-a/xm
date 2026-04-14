package com.example.service;

import com.example.entity.Academy;
import com.example.mapper.AcademyMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AcademyService {

    @Resource
    private AcademyMapper academyMapper;

    public void add(Academy academy) {
        academyMapper.insert(academy);
    }

    public void update(Academy academy) {
        academyMapper.updateById(academy);
    }

    public void deleteById(Integer id) {
        academyMapper.deleteById(id);
    }

    public List<Academy> selectAll(Academy academy) {
//        List<Employee> list = employeeMapper.selectAll();
//        return list;
        return academyMapper.selectAll(academy);
    }

    public Academy selectById(Integer id) {
        return academyMapper.selectById(id);
    }

    public PageInfo<Academy> selectPage(Academy academy, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum,pageSize);
        List<Academy> list = academyMapper.selectAll(academy);
        return PageInfo.of(list);
    }


    public void deleteBatch(List<Integer> ids) {
        for (Integer id : ids) {
            this.deleteById(id);
        }
    }

    // 新增：根据学院名称查询学院ID（供导入用户时使用）
    public Integer findIdByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null; // 名称为空时返回null
        }
        Academy academy = academyMapper.selectByName(name.trim());
        return academy != null ? academy.getId() : null; // 存在则返回ID，否则返回null
    }

}
