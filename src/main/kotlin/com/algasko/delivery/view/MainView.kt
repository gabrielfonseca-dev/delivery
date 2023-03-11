package com.algasko.delivery.view

import com.algasko.delivery.data.entity.Delivery
import com.algasko.delivery.data.entity.Volume
import com.algasko.delivery.data.repository.DeliveryRepository
import com.algasko.delivery.security.SecurityService
import com.algasko.delivery.util.BarcodeImageDecoder
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.formlayout.FormLayout
import com.vaadin.flow.component.html.*
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.tabs.Tab
import com.vaadin.flow.component.tabs.Tabs
import com.vaadin.flow.component.upload.SucceededEvent
import com.vaadin.flow.component.upload.Upload
import com.vaadin.flow.component.upload.UploadI18N
import com.vaadin.flow.component.upload.UploadI18N.*
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import com.vaadin.flow.spring.annotation.SpringComponent
import com.vaadin.flow.spring.annotation.UIScope
import org.springframework.context.annotation.ComponentScan
import java.net.URLEncoder
import java.util.*


@PageTitle("Entregas")
@ComponentScan("home-view")
@Route(value = "", layout = MainLayout::class)
@SpringComponent
@UIScope
class MainView(val deliveryRepository: DeliveryRepository) : VerticalLayout() {

    private val tabView = Tab(Icon(VaadinIcon.PACKAGE), Span("Entregas"))
    private val tabs = Tabs(tabView)
    private val content = VerticalLayout()

    init {
        this.tabs.orientation = Tabs.Orientation.HORIZONTAL
        this.tabs.addSelectedChangeListener { event -> this.setContent(event.selectedTab) }
        this.defaultHorizontalComponentAlignment = FlexComponent.Alignment.CENTER
        this.style["text-align"] = "center"
        this.setContent(tabs.selectedTab)
        this.add(tabs, content)
        this.setSizeFull()
    }

    private fun setContent(tab: Tab) {
        this.content.removeAll()
        when (tab) {
            this.tabView -> {
                this.content.add(createTabView())
            }
        }
    }

    private fun createTabView(): VerticalLayout {
        val cardLayout = FormLayout()
        val deliveries = deliveryRepository.findByUser(SecurityService().authenticatedUser ?: "")
        deliveries?.forEach { delivery ->
            cardLayout.add(DeliveryComponent(delivery, deliveryRepository))
        }
        cardLayout.setResponsiveSteps(
            FormLayout.ResponsiveStep("0px", 1),
            FormLayout.ResponsiveStep("600px", 2),
            FormLayout.ResponsiveStep("900px", 3)
        )
        val tabView = VerticalLayout()
        tabView.add(cardLayout)
        tabView.defaultHorizontalComponentAlignment = FlexComponent.Alignment.CENTER
        return tabView
    }

    class DeliveryComponent(private val delivery: Delivery, private val deliveryRepository: DeliveryRepository) :
        VerticalLayout() {

        private val apiKey = "AIzaSyBFw0Qbyq9zTFTd-tUY6dZWTgaQzuU17R8"
        private val buffer = MultiFileMemoryBuffer()

        init {
            this.add(createHeader(), createMap(), createFooter(), creteMenu())
            this.addClassNames("card", "hover")
            this.width = "300px"
            this.isPadding = false
            this.isSpacing = false
        }

        private fun createHeader(): VerticalLayout {
            return VerticalLayout(
                HorizontalLayout(Icon(VaadinIcon.USER), H5(delivery.customerName)),
                H6("Pedido: ${delivery.orderNumber} | Rastreio: ${delivery.code}")
            )
        }

        private fun createMap(): IFrame {
            val iframe = IFrame(
                "https://www.google.com/maps/embed/v1/place?q=${
                    URLEncoder.encode(
                        delivery.address,
                        Charsets.UTF_8
                    )
                }&key=${this.apiKey}"
            )
            iframe.addClassName("frame")
            return iframe
        }

        private fun createFooter(): VerticalLayout {
            return VerticalLayout(
                HorizontalLayout(Icon(VaadinIcon.PHONE), H5(delivery.customerPhone)),
                HorizontalLayout(Icon(VaadinIcon.PIN), H6(delivery.address))
            )
        }

        private fun creteMenu(): HorizontalLayout {
            return HorizontalLayout(
                Button("Entrar em contato", Icon(VaadinIcon.CHAT)).apply {
                    this.addClickListener {

                    }
                    this.isEnabled = false
                },
                Button("Entregar", Icon(VaadinIcon.PACKAGE)).apply {
                    this.addClickListener {
                        Dialog(createModal()).apply {
                            this.open()
                            this.addDialogCloseActionListener {
                                this.close()
                                UI.getCurrent().page.reload()
                            }
                        }
                    }
                }
            ).apply {
                this.defaultVerticalComponentAlignment = FlexComponent.Alignment.CENTER
                this.justifyContentMode = FlexComponent.JustifyContentMode.CENTER
                this.setWidthFull()
            }
        }

        private fun createModal(): FormLayout {

            val container = FormLayout()
            val refList: MutableList<VolumeRef> = ArrayList()

            delivery.volumes?.forEach { volume ->
                container.add(VerticalLayout(
                    Label("Rastreio: ${delivery.code}"),
                    Label("Volume: ${volume.code}"),
                    Label("EAN: ${volume.barcode}"),
                    Label("Status: ${if (volume.confirmed) "Confirmado" else "Pendente"}").apply {
                        refList.add(VolumeRef(volume, this))
                    },
                    Image(volume.productImgUrl, "").apply {
                        this.width = "20%"
                        this.height = "20%"
                        this.addClickListener { UI.getCurrent().page.open(volume.productImgUrl) }
                    }
                ).apply {
                    this.addClassName("card")
                })
            }

            container.add(
                Upload(buffer).apply {
                this.addSucceededListener { event: SucceededEvent ->
                    val fileName = event.fileName
                    BarcodeImageDecoder().decodeImage(buffer.getInputStream(fileName)).run {
                        delivery.volumes?.forEach { volume ->
                            if (this.text == volume.code || this.text == volume.barcode) {
                                volume.confirmed = true
                                Notification.show("Volume ${volume.code} confirmado!", 5000, Notification.Position.TOP_CENTER)
                                refList.find { it.volume == volume }?.label?.text = "Status: Confirmado"
                                UI.getCurrent().push()
                            }
                        }
                    }
                    this.clearFileList()
                    delivery.volumes?.find { !it.confirmed } ?: run {
                        delivery.close()
                        deliveryRepository.save(delivery)
                        Notification.show("Entrega ${delivery.code} realizada!", 5000, Notification.Position.TOP_CENTER)
                    }
                }
                this.i18n = I18n()
            })

            return container
        }

        class VolumeRef(val volume: Volume, val label: Label)

        class I18n : UploadI18N() {
            init {
                dropFiles = DropFiles().setOne("Arraste os arquivos")
                    .setMany("Arraste os arquivos")
                addFiles = AddFiles().setOne("Enviar Códigos de Barras")
                    .setMany("Enviar Códigos de Barras")
            }
        }

    }
}