package ua.com.solidity.web.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ua.com.solidity.db.entities.Role;
import ua.com.solidity.db.entities.RoleMap;
import ua.com.solidity.db.repositories.RoleMapRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleService {

	private final RoleMapRepository roleMapRepository;

	public Role getRoleFromMemberOf(ArrayList<String> memberOf) {
		System.out.println("Login attempt -> " + memberOf.toString());
		List<String> personGroups = memberOf
				.stream()
				.map((s) -> s.substring(s.indexOf("=") + 1, s.indexOf(",")))
				.collect(Collectors.toList());
		List<RoleMap> roleMaps = roleMapRepository.findAllById(personGroups);
		System.out.println("Role maps " + roleMaps.toString());
		Role role = null;

		if (!roleMaps.isEmpty()) {
			roleMaps.sort(Comparator.comparingInt(i -> i.getRole().getId()));
			role = roleMaps.get(0).getRole();
		}
		System.out.println("Role -> " + role.toString());
		return role;
	}
}
