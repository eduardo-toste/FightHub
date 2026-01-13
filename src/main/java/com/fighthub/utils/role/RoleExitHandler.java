package com.fighthub.utils.role;

import com.fighthub.model.Usuario;
import com.fighthub.model.enums.Role;

public interface RoleExitHandler {

    Role getSourceRole();
    void onExit(Usuario usuario);

}
