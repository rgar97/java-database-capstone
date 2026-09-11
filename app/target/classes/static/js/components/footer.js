/**
 * Componente de pie de página estático y reutilizable
 */
function renderFooter() {
    const footer = document.getElementById("footer");
    
    // Verificar que el elemento exista antes de intentar inyectar HTML
    if (!footer) return;

    footer.innerHTML = `
        <footer class="footer">
            <div class="footer-brand">
                <p>&copy; Copyright 2026 Clinic Management System. Todos los derechos reservados.</p>
            </div>
            <div class="footer-columns">
                <div class="footer-column">
                    <h4>Empresa</h4>
                    <a href="#">Acerca de</a>
                    <a href="#">Carreras</a>
                    <a href="#">Prensa</a>
                </div>
                <div class="footer-column">
                    <h4>Soporte</h4>
                    <a href="#">Cuenta</a>
                    <a href="#">Centro de Ayuda</a>
                    <a href="#">Contacto</a>
                </div>
                <div class="footer-column">
                    <h4>Legales</h4>
                    <a href="#">Términos</a>
                    <a href="#">Política de Privacidad</a>
                    <a href="#">Licencias</a>
                </div>
            </div>
        </footer>
    `;
}

// Ejecutar la función automáticamente al cargar el script o mediante el evento DOMContentLoaded
if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", renderFooter);
} else {
    renderFooter();
}