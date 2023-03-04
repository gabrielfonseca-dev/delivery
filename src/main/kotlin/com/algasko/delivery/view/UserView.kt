package com.algasko.delivery.view

import com.algasko.delivery.controller.UserController
import com.algasko.delivery.data.entity.Role
import com.algasko.delivery.data.entity.User
import com.algasko.delivery.data.enum.Privileges
import com.algasko.delivery.data.repository.PermissionRepository
import com.algasko.delivery.data.repository.RoleRepository
import com.algasko.delivery.security.AppSecurity
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.Unit
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.checkbox.Checkbox
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.formlayout.FormLayout
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.html.H3
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.Scroller
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.EmailField
import com.vaadin.flow.component.textfield.PasswordField
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.data.binder.Binder
import com.vaadin.flow.data.validator.BeanValidator
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import com.vaadin.flow.spring.annotation.SpringComponent
import com.vaadin.flow.spring.annotation.UIScope
import org.vaadin.textfieldformatter.CustomStringBlockFormatter

@PageTitle("Usuários")
@Route(value = "admin/users", layout = MainLayout::class)
@SpringComponent
@UIScope
class UserView(
    private val userController: UserController,
    private val roleRepository: RoleRepository,
    permissionRepository: PermissionRepository
) : VerticalLayout(), AppSecurity {
    private var user: User? = null
    private var role: Role? = null
    private val permissionList = permissionRepository.findAll()
    private val roleList = roleRepository.findAll()
    private val userBinder = Binder(User::class.java)
    private val userTable = Grid(User::class.java)
    private val userModal = Dialog()
    private val roleModal = Dialog()
    private val newRoleModal = Dialog()
    private val roleComboBox = ComboBox("Perfil", roleList)
    private val permissionBox = VerticalLayout()
    private val notificationPosition = Notification.Position.TOP_END

    init {
        this.add(
            createSearch(),
            createMenu(),
            createTable(),
            createModal(),
            createRoleModal()
        )
        this.setSizeFull()
        justifyContentMode = FlexComponent.JustifyContentMode.CENTER
        defaultHorizontalComponentAlignment = FlexComponent.Alignment.CENTER
        style["text-align"] = "center"
    }

    private fun createTable(): Scroller {
        userTable.addItemClickListener {
            user = it.item
            userBinder.readBean(user)
            userModal.open()
        }
        userTable.setColumns("name", "username", "email", "document", "role")
        userTable.columns[0].setHeader("Nome")
        userTable.columns[1].setHeader("Login")
        userTable.columns[2].setHeader("E-Mail")
        userTable.columns[3].setHeader("CPF")
        userTable.columns[4].setHeader("Perfil")
        userTable.setSelectionMode(Grid.SelectionMode.SINGLE)
        userTable.isRowsDraggable = true
        userTable.isColumnReorderingAllowed = true
        userTable.isVerticalScrollingEnabled = true
        userTable.setWidthFull()
        userTable.setHeight(95F, Unit.PERCENTAGE)
        val tableScroller = Scroller(userTable)
        tableScroller.setSizeFull()
        return tableScroller
    }

    private fun createMenu(): HorizontalLayout {
        val newUser = Button("Novo", Icon(VaadinIcon.PLUS))
        newUser.addClickListener {
            user = User()
            userBinder.readBean(user)
            userModal.open()
        }
        val permissionsBtn = Button("Permissões", Icon(VaadinIcon.CURLY_BRACKETS))
        permissionsBtn.addClickListener {
            roleModal.open()
        }
        val btnLayout = HorizontalLayout()
        btnLayout.setWidthFull()
        btnLayout.add(permissionsBtn, newUser)
        btnLayout.justifyContentMode = FlexComponent.JustifyContentMode.END
        btnLayout.defaultVerticalComponentAlignment = FlexComponent.Alignment.END
        return btnLayout
    }

    private fun createSearch(): FormLayout {
        val searchView = FormLayout()
        val searchField = TextField()
        searchField.placeholder = "Digite para buscar"
        val btnSearch = Button("Buscar", Icon("search"))
        val inactiveUsers = Checkbox("Inativos")
        btnSearch.addClickListener {
            userTable.setItems(userController.search(searchField.value, inactiveUsers.value).toMutableList())
        }
        val optionsGroup = HorizontalLayout(btnSearch, inactiveUsers)
        optionsGroup.defaultVerticalComponentAlignment = FlexComponent.Alignment.CENTER
        searchView.add(searchField, optionsGroup)
        return searchView
    }

    private fun updatePermissionView() {
        permissionBox.removeAll()
        createRolePermissionView()
    }

    private fun createRolePermissionView() {
        val form = FormLayout()
        permissionList.forEach { permission ->
            val hasPermission: Boolean =
                (roleComboBox.value.permissions.find { rolePermission -> rolePermission?.code == permission.code } != null)
            val checkBox = Checkbox(permission.code, hasPermission)
            form.add(checkBox)

            checkBox.addValueChangeListener { _ ->
                if (checkBox.value == true) {
                    if (role?.permissions?.find { p -> p?.code == permission.code } == null) {
                        role?.permissions?.add(permission)
                    }
                } else {
                    role?.permissions?.removeIf { it?.code == permission.code }
                }
            }
        }
        permissionBox.add(form)
    }

    private fun createNewRoleModal() {
        val roleName = TextField("Nome do Perfil")
        val saveRole = Button("", Icon("check-circle"))
        saveRole.addClickListener {
            if ( roleName.value.isNotEmpty() && this.hasPrivilege(Privileges.WRITE, userController.userRepository) ) {
                role = Role()
                role!!.name = roleName.value
                roleRepository.save(role!!)
                newRoleModal.close()
            } else {
                Notification.show("Valor inválido ou você não possui permissão", 5000, notificationPosition)
            }
        }
        val div = HorizontalLayout(roleName, saveRole)
        div.defaultVerticalComponentAlignment = FlexComponent.Alignment.BASELINE
        newRoleModal.add(div)
        newRoleModal.isModal = true
    }

    private fun createRoleModal(): Dialog {
        roleModal.add(H3("Perfis de Acesso"))
        roleComboBox.value = roleList.first()
        role = roleComboBox.value
        roleModal.add(newRoleModal)

        createNewRoleModal()
        val newRole = Button("Novo", Icon("plus"))
        newRole.addClickListener { newRoleModal.open() }

        val roleFilter = FormLayout(roleComboBox, newRole)

        createRolePermissionView()
        roleComboBox.addValueChangeListener {
            role = roleList.find { role -> role.name == roleComboBox.value.name }
            updatePermissionView()
        }
        roleModal.add(roleFilter, permissionBox)

        val cancel = Button("Cancelar", Icon("close-circle"))
        cancel.addClickListener { roleModal.close() }
        val save = Button("Salvar", Icon("check-circle"))
        save.addClickListener {
            if (this.hasPrivilege(Privileges.WRITE, userController.userRepository)) {
                roleRepository.save(role!!)
                roleModal.close()
                UI.getCurrent().push()
            } else {
                Notification.show("You don't have permission!", 5000, notificationPosition)
            }
        }

        val btnGrp = FormLayout()
        btnGrp.add(cancel, save)

        roleModal.add(btnGrp)
        roleModal.isModal = true
        roleModal.isResizable = true
        roleModal.isDraggable = true
        return roleModal
    }

    private fun createModal(): Dialog {
        val name = TextField("Nome")
        name.isRequiredIndicatorVisible = true
        userBinder.forField(name)
            .withValidator(BeanValidator(User::class.java, "name"))
            .bind(User::name, User::name.setter)
            .validate(true)

        val username = TextField("Login")
        username.isRequiredIndicatorVisible = true
        userBinder.forField(username)
            .withValidator(BeanValidator(User::class.java, "username"))
            .bind(User::username, User::username.setter)
            .validate(true)

        val email = EmailField("E-Mail")
        email.isRequiredIndicatorVisible = true
        userBinder.forField(email)
            .withValidator(BeanValidator(User::class.java, "email"))
            .bind(User::email, User::email.setter)
            .validate(true)

        val documentField = TextField("CPF")
        documentField.isRequiredIndicatorVisible = true
        userBinder.forField(documentField)
            .withValidator(BeanValidator(User::class.java, "document"))
            .bind(User::document, User::document.setter)
            .validate(true)

        val optionsCpf = CustomStringBlockFormatter.Options()
        optionsCpf.setBlocks(3, 3, 3, 2)
        optionsCpf.isNumericOnly = true
        optionsCpf.setDelimiters(".", "-")
        CustomStringBlockFormatter(optionsCpf).extend(documentField)

        val password = PasswordField("Senha")
        password.isRequiredIndicatorVisible = true
        password.isRevealButtonVisible = true
        password.isReadOnly = true
        password.setWidthFull()
        userBinder.forField(password)
            .withValidator(BeanValidator(User::class.java, "password"))
            .bind({ "" }, User::password.setter)
            .validate(true)

        val passGen = Button("Gerar")
        passGen.addClickListener { password.value = this.generatePassword() }

        val passwordDiv = HorizontalLayout(password, passGen)
        passwordDiv.setSizeFull()
        passwordDiv.justifyContentMode = FlexComponent.JustifyContentMode.BETWEEN
        passwordDiv.defaultVerticalComponentAlignment = FlexComponent.Alignment.BASELINE

        val enabled = Checkbox("Ativo")
        userBinder.forField(enabled)
            .withValidator(BeanValidator(User::class.java, "enabled"))
            .bind(User::enabled, User::enabled.setter)
            .validate(true)

        val driver = Checkbox("Motorista")
        userBinder.forField(driver)
            .withValidator(BeanValidator(User::class.java, "driver"))
            .bind(User::driver, User::driver.setter)
            .validate(true)

        val checkBoxDiv = HorizontalLayout()
        checkBoxDiv.add(enabled, driver)
        checkBoxDiv.setWidthFull()
        checkBoxDiv.justifyContentMode = FlexComponent.JustifyContentMode.CENTER

        val phone = TextField("Telefone")
        userBinder.forField(phone)
            .withValidator(BeanValidator(User::class.java, "phone"))
            .bind(User::phone, User::phone.setter)
            .validate(true)

        CustomStringBlockFormatter.Builder()
            .blocks(0, 2, 5, 4)
            .delimiters("(", ")", "-")
            .numeric().build().extend(phone)

        val group = ComboBox("Grupo", roleRepository.findAll())
        group.isAllowCustomValue = false
        userBinder.forField(group)
            .withValidator(BeanValidator(User::class.java, "role"))
            .bind(User::role, User::role.setter)
            .validate(true)

        userModal.addDialogCloseActionListener {
            user = User()
            userBinder.readBean(user)
            userModal.close()
        }

        val cancel = Button("Fechar", Icon("close-circle")) { userModal.close() }

        val save = Button("Salvar", Icon("check-circle")) {
            try {
                if (password.value.isEmpty() && !user?.password.isNullOrBlank()) userBinder.removeBinding(password)
                if (!userBinder.validate().hasErrors()) {
                    userBinder.writeBean(user)
                    userController.save(user!!)
                    userTable.setItems(userController.search(user?.username.toString(), false).toMutableList())
                    userModal.close()
                    Notification.show("Sucesso", 5000, notificationPosition)
                }
            } catch (v: Exception) {
                if (this.userExists(name.value, username.value, email.value, documentField.value, userController.userRepository)) {
                    Notification.show("Usuário já existe", 5000, notificationPosition)
                } else {
                    Notification.show(v.message ?: "", 5000, notificationPosition)
                }
            }
        }

        val btnGrp = HorizontalLayout()
        btnGrp.setWidthFull()
        btnGrp.add(cancel, save)
        btnGrp.justifyContentMode = FlexComponent.JustifyContentMode.CENTER

        val userForm = FormLayout()
        userForm.add(name, username, email, documentField, passwordDiv, phone, group, checkBoxDiv)
        userModal.add(H3("Usuário"), userForm, btnGrp)
        userModal.isModal = true
        userModal.isResizable = true
        userModal.isDraggable = true
        return userModal
    }

}
