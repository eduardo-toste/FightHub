package com.fighthub.utils.role;

import com.fighthub.model.Usuario;
import com.fighthub.model.enums.Role;

public interface RoleEnterHandler {

    Role getTargetRole();
    void onEnter(Usuario usuario);

}
