package com.miniproject.service;

import com.miniproject.common.BusinessException;
import com.miniproject.common.ErrorCode;
import com.miniproject.domain.Role;
import com.miniproject.domain.RoleRepository;
import com.miniproject.domain.UserRoleRepository;
import com.miniproject.domain.MenuRoleRepository;
import com.miniproject.domain.MenuRoleButtonRepository;
import com.miniproject.dto.CreateRoleRequest;
import com.miniproject.dto.RoleResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RoleService {

    public static final String SYSTEM_ADMIN_CODE = "SYSTEM_ADMIN";
    public static final String USER_CODE = "USER";

    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final MenuRoleRepository menuRoleRepository;
    private final MenuRoleButtonRepository menuRoleButtonRepository;

    public RoleService(RoleRepository roleRepository,
                       UserRoleRepository userRoleRepository,
                       MenuRoleRepository menuRoleRepository,
                       MenuRoleButtonRepository menuRoleButtonRepository) {
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.menuRoleRepository = menuRoleRepository;
        this.menuRoleButtonRepository = menuRoleButtonRepository;
    }

    @Transactional(readOnly = true)
    public List<RoleResponse> getAllRoles() {
        return roleRepository.findAllByOrderByIdAsc().stream()
                .map(RoleResponse::from)
                .toList();
    }

    @Transactional
    public RoleResponse createRole(CreateRoleRequest request) {
        String code = request.getCode().trim().toUpperCase();
        if (roleRepository.existsByCode(code)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "이미 존재하는 Role 코드입니다.");
        }
        Role role = roleRepository.save(new Role(code, request.getName().trim(), request.getDescription()));
        return RoleResponse.from(role);
    }

    @Transactional
    public void deleteRole(Long roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Role을 찾을 수 없습니다."));
        userRoleRepository.deleteByRoleId(roleId);
        menuRoleButtonRepository.deleteByRoleId(roleId);
        menuRoleRepository.deleteByRoleId(roleId);
        roleRepository.delete(role);
    }

    @Transactional(readOnly = true)
    public boolean isSystemAdmin(String email) {
        return userRoleRepository.existsByUser_EmailAndRole_Code(email, SYSTEM_ADMIN_CODE);
    }

    @Transactional(readOnly = true)
    public Role getRequiredRole(Long roleId) {
        return roleRepository.findById(roleId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Role을 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public Role getByCode(String code) {
        return roleRepository.findByCode(code)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Role을 찾을 수 없습니다."));
    }
}
