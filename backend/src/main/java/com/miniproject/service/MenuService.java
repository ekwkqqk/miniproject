package com.miniproject.service;

import com.miniproject.common.BusinessException;
import com.miniproject.common.ErrorCode;
import com.miniproject.domain.Menu;
import com.miniproject.domain.MenuRepository;
import com.miniproject.domain.MenuRole;
import com.miniproject.domain.MenuRoleButton;
import com.miniproject.domain.MenuRoleButtonRepository;
import com.miniproject.domain.MenuRoleRepository;
import com.miniproject.domain.Role;
import com.miniproject.domain.UserRoleRepository;
import com.miniproject.dto.ButtonPermissionDto;
import com.miniproject.dto.MenuRequest;
import com.miniproject.dto.MenuResponse;
import com.miniproject.dto.RoleResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class MenuService {

    private final MenuRepository menuRepository;
    private final MenuRoleRepository menuRoleRepository;
    private final MenuRoleButtonRepository menuRoleButtonRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleService roleService;

    public MenuService(MenuRepository menuRepository,
                       MenuRoleRepository menuRoleRepository,
                       MenuRoleButtonRepository menuRoleButtonRepository,
                       UserRoleRepository userRoleRepository,
                       RoleService roleService) {
        this.menuRepository = menuRepository;
        this.menuRoleRepository = menuRoleRepository;
        this.menuRoleButtonRepository = menuRoleButtonRepository;
        this.userRoleRepository = userRoleRepository;
        this.roleService = roleService;
    }

    @Transactional(readOnly = true)
    public List<MenuResponse> getAllMenus() {
        List<Menu> menus = menuRepository.findAllByOrderBySortOrderAscIdAsc();
        Map<Long, MenuResponse> nodeMap = new HashMap<>();
        for (Menu menu : menus) {
            nodeMap.put(menu.getId(), toAdminNode(menu));
        }
        return buildTree(menus, nodeMap);
    }

    @Transactional(readOnly = true)
    public List<MenuResponse> getMyMenus(String email) {
        List<Role> roles = userRoleRepository.findRolesByUserEmail(email);
        if (roles.isEmpty()) {
            return List.of();
        }
        List<Long> roleIds = roles.stream().map(Role::getId).toList();
        Set<Long> accessibleLeafIds = menuRoleRepository.findMenusByRoleIds(roleIds).stream()
                .filter(menu -> !menu.isFolder())
                .map(Menu::getId)
                .collect(Collectors.toSet());

        if (accessibleLeafIds.isEmpty()) {
            return List.of();
        }

        List<Menu> allMenus = menuRepository.findAllByOrderBySortOrderAscIdAsc();
        Map<Long, Menu> menuById = allMenus.stream()
                .collect(Collectors.toMap(Menu::getId, Function.identity()));

        Set<Long> includeIds = new HashSet<>(accessibleLeafIds);
        for (Long leafId : accessibleLeafIds) {
            Menu current = menuById.get(leafId);
            while (current != null && current.getParentId() != null) {
                includeIds.add(current.getParentId());
                current = menuById.get(current.getParentId());
            }
        }

        List<Menu> includedMenus = allMenus.stream()
                .filter(menu -> includeIds.contains(menu.getId()))
                .toList();

        Map<Long, MenuResponse> nodeMap = new HashMap<>();
        for (Menu menu : includedMenus) {
            if (menu.isFolder()) {
                nodeMap.put(menu.getId(), toFolderNode(menu));
            } else {
                nodeMap.put(menu.getId(), toMyLeafNode(menu, roleIds));
            }
        }
        return buildTree(includedMenus, nodeMap);
    }

    @Transactional(readOnly = true)
    public boolean canAccessMenu(String email, String url) {
        return flattenLeaves(getMyMenus(email)).stream()
                .anyMatch(menu -> url.equals(menu.getUrl()));
    }

    @Transactional
    public MenuResponse createMenu(MenuRequest request) {
        Menu parent = resolveParent(request.getParentId(), null);
        boolean folder = isBlank(request.getUrl());
        String url = folder ? null : normalizeUrl(request.getUrl());

        if (!folder && menuRepository.existsByUrl(url)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "이미 존재하는 메뉴 URL입니다.");
        }

        int sortOrder = request.getSortOrder() != null ? request.getSortOrder() : nextSortOrder(request.getParentId());
        Menu menu = menuRepository.save(new Menu(request.getName().trim(), url, sortOrder, parent));
        applyRolesAndButtons(menu, request, folder);
        return findAdminTreeNode(menu.getId());
    }

    @Transactional
    public MenuResponse updateMenu(Long menuId, MenuRequest request) {
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "메뉴를 찾을 수 없습니다."));

        Menu parent = resolveParent(request.getParentId(), menuId);
        validateNoCycle(menuId, parent);

        boolean folder = isBlank(request.getUrl());
        String url = folder ? null : normalizeUrl(request.getUrl());

        if (!folder) {
            menuRepository.findByUrl(url).ifPresent(existing -> {
                if (!existing.getId().equals(menuId)) {
                    throw new BusinessException(ErrorCode.INVALID_INPUT, "이미 존재하는 메뉴 URL입니다.");
                }
            });
        }

        if (!folder && menuRepository.existsByParent_Id(menuId)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "하위 메뉴가 있는 항목은 URL이 있는 화면 메뉴로 변경할 수 없습니다. 먼저 하위를 정리하세요.");
        }

        menu.changeParent(parent);
        menu.changeName(request.getName().trim());
        menu.changeUrl(url);
        if (request.getSortOrder() != null) {
            menu.changeSortOrder(request.getSortOrder());
        }

        menuRoleButtonRepository.deleteByMenuId(menuId);
        menuRoleRepository.deleteByMenuId(menuId);
        applyRolesAndButtons(menu, request, folder);
        return findAdminTreeNode(menuId);
    }

    @Transactional
    public void deleteMenu(Long menuId) {
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "메뉴를 찾을 수 없습니다."));
        if (menuRepository.existsByParent_Id(menuId)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "하위 메뉴가 있어 삭제할 수 없습니다. 하위 메뉴를 먼저 삭제하세요.");
        }
        menuRoleButtonRepository.deleteByMenuId(menuId);
        menuRoleRepository.deleteByMenuId(menuId);
        menuRepository.delete(menu);
    }

    private void applyRolesAndButtons(Menu menu, MenuRequest request, boolean folder) {
        if (folder) {
            return;
        }
        Set<Long> roleIds = new HashSet<>(request.getRoleIds() == null ? List.of() : request.getRoleIds());
        if (roleIds.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "화면 메뉴에는 Role을 하나 이상 지정해주세요.");
        }

        Map<Long, MenuRequest.MenuRoleButtonRequest> buttonMap = (request.getRoleButtons() == null ? List.<MenuRequest.MenuRoleButtonRequest>of() : request.getRoleButtons())
                .stream()
                .collect(Collectors.toMap(MenuRequest.MenuRoleButtonRequest::getRoleId, Function.identity(), (a, b) -> b));

        for (Long roleId : roleIds) {
            Role role = roleService.getRequiredRole(roleId);
            menuRoleRepository.save(new MenuRole(menu, role));

            MenuRequest.MenuRoleButtonRequest buttonReq = buttonMap.get(roleId);
            boolean canRead = buttonReq == null || buttonReq.isCanRead();
            boolean canUpdate = buttonReq != null && buttonReq.isCanUpdate();
            boolean canDelete = buttonReq != null && buttonReq.isCanDelete();
            boolean canUpload = buttonReq != null && buttonReq.isCanUpload();
            boolean canDownload = buttonReq != null && buttonReq.isCanDownload();
            boolean canOther = buttonReq != null && buttonReq.isCanOther();

            menuRoleButtonRepository.save(new MenuRoleButton(
                    menu, role, canRead, canUpdate, canDelete, canUpload, canDownload, canOther
            ));
        }
    }

    private Menu resolveParent(Long parentId, Long selfId) {
        if (parentId == null) {
            return null;
        }
        if (Objects.equals(parentId, selfId)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "자기 자신을 상위 메뉴로 지정할 수 없습니다.");
        }
        return menuRepository.findById(parentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "상위 메뉴를 찾을 수 없습니다."));
    }

    private void validateNoCycle(Long menuId, Menu parent) {
        Menu current = parent;
        while (current != null) {
            if (current.getId().equals(menuId)) {
                throw new BusinessException(ErrorCode.INVALID_INPUT, "하위 메뉴를 상위 메뉴로 지정할 수 없습니다.");
            }
            Long parentId = current.getParentId();
            current = parentId == null ? null : menuRepository.findById(parentId).orElse(null);
        }
    }

    private List<MenuResponse> buildTree(List<Menu> menus, Map<Long, MenuResponse> nodeMap) {
        List<MenuResponse> roots = new ArrayList<>();
        Map<Long, List<MenuResponse>> childrenMap = new HashMap<>();

        for (Menu menu : menus) {
            MenuResponse node = nodeMap.get(menu.getId());
            Long parentId = menu.getParentId();
            if (parentId == null || !nodeMap.containsKey(parentId)) {
                roots.add(node);
            } else {
                childrenMap.computeIfAbsent(parentId, key -> new ArrayList<>()).add(node);
            }
        }

        for (Map.Entry<Long, List<MenuResponse>> entry : childrenMap.entrySet()) {
            MenuResponse parent = nodeMap.get(entry.getKey());
            if (parent != null) {
                parent.getChildren().addAll(entry.getValue().stream()
                        .sorted(Comparator.comparingInt(MenuResponse::getSortOrder).thenComparing(MenuResponse::getId))
                        .toList());
            }
        }

        roots.sort(Comparator.comparingInt(MenuResponse::getSortOrder).thenComparing(MenuResponse::getId));
        return roots;
    }

    private MenuResponse findAdminTreeNode(Long menuId) {
        return flattenAll(getAllMenus()).stream()
                .filter(menu -> menu.getId().equals(menuId))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "메뉴를 찾을 수 없습니다."));
    }

    private MenuResponse toAdminNode(Menu menu) {
        List<RoleResponse> roles = List.of();
        List<MenuResponse.MenuRoleButtonResponse> roleButtons = List.of();
        if (!menu.isFolder()) {
            roles = menuRoleRepository.findByMenuId(menu.getId()).stream()
                    .map(MenuRole::getRole)
                    .map(RoleResponse::from)
                    .toList();
            roleButtons = menuRoleButtonRepository.findByMenuId(menu.getId()).stream()
                    .map(button -> new MenuResponse.MenuRoleButtonResponse(
                            button.getRole().getId(),
                            button.getRole().getCode(),
                            button.getRole().getName(),
                            toButtonDto(button)
                    ))
                    .toList();
        }
        return new MenuResponse(
                menu.getId(),
                menu.getParentId(),
                menu.getName(),
                menu.getUrl(),
                menu.getSortOrder(),
                menu.isFolder(),
                roles,
                roleButtons,
                ButtonPermissionDto.none(),
                new ArrayList<>()
        );
    }

    private MenuResponse toFolderNode(Menu menu) {
        return new MenuResponse(
                menu.getId(),
                menu.getParentId(),
                menu.getName(),
                null,
                menu.getSortOrder(),
                true,
                List.of(),
                List.of(),
                ButtonPermissionDto.none(),
                new ArrayList<>()
        );
    }

    private MenuResponse toMyLeafNode(Menu menu, List<Long> roleIds) {
        List<ButtonPermissionDto> buttons = menuRoleButtonRepository.findByMenuIdAndRoleIdIn(menu.getId(), roleIds)
                .stream()
                .map(this::toButtonDto)
                .toList();
        return new MenuResponse(
                menu.getId(),
                menu.getParentId(),
                menu.getName(),
                menu.getUrl(),
                menu.getSortOrder(),
                false,
                List.of(),
                List.of(),
                ButtonPermissionDto.merge(buttons),
                new ArrayList<>()
        );
    }

    private ButtonPermissionDto toButtonDto(MenuRoleButton button) {
        return new ButtonPermissionDto(
                button.isCanRead(),
                button.isCanUpdate(),
                button.isCanDelete(),
                button.isCanUpload(),
                button.isCanDownload(),
                button.isCanOther()
        );
    }

    private List<MenuResponse> flattenAll(List<MenuResponse> roots) {
        List<MenuResponse> result = new ArrayList<>();
        for (MenuResponse root : roots) {
            result.add(root);
            result.addAll(flattenAll(root.getChildren()));
        }
        return result;
    }

    private List<MenuResponse> flattenLeaves(List<MenuResponse> roots) {
        List<MenuResponse> result = new ArrayList<>();
        for (MenuResponse node : roots) {
            if (!node.isFolder() && node.getUrl() != null) {
                result.add(node);
            }
            result.addAll(flattenLeaves(node.getChildren()));
        }
        return result;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String normalizeUrl(String url) {
        String trimmed = url.trim();
        if (!trimmed.startsWith("/")) {
            trimmed = "/" + trimmed;
        }
        if (trimmed.length() > 1 && trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }

    private int nextSortOrder(Long parentId) {
        List<Menu> siblings = parentId == null
                ? menuRepository.findAllByOrderBySortOrderAscIdAsc().stream()
                .filter(menu -> menu.getParentId() == null)
                .toList()
                : menuRepository.findByParent_IdOrderBySortOrderAscIdAsc(parentId);
        return siblings.stream()
                .mapToInt(Menu::getSortOrder)
                .max()
                .orElse(0) + 1;
    }
}
