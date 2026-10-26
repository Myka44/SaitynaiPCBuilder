import { Route, Routes } from 'react-router-dom'
import { ProtectedRoute } from './auth/ProtectedRoute'
import { Layout } from './components/Layout'
import { AdminConfigurationsPage } from './pages/AdminConfigurationsPage'
import { AuthPage } from './pages/AuthPage'
import { CatalogPage } from './pages/CatalogPage'
import { ComponentDetailPage } from './pages/ComponentDetailPage'
import { ConfigurationDetailPage } from './pages/ConfigurationDetailPage'
import { ConfigurationEditorPage } from './pages/ConfigurationEditorPage'
import { HomePage } from './pages/HomePage'
import { MyConfigurationsPage } from './pages/MyConfigurationsPage'
import { StatusPage } from './pages/StatusPage'

export default function App() {
  return <Routes><Route element={<Layout />}>
    <Route index element={<HomePage />} />
    <Route path="catalog" element={<CatalogPage />} />
    <Route path="catalog/:id" element={<ComponentDetailPage />} />
    <Route path="configurations/:id" element={<ConfigurationDetailPage />} />
    <Route path="login" element={<AuthPage mode="login" />} />
    <Route path="register" element={<AuthPage mode="register" />} />
    <Route path="forbidden" element={<StatusPage forbidden />} />
    <Route element={<ProtectedRoute />}>
      <Route path="app/configurations" element={<MyConfigurationsPage />} />
      <Route path="app/configurations/:id/edit" element={<ConfigurationEditorPage />} />
    </Route>
    <Route element={<ProtectedRoute role="ADMIN" />}>
      <Route path="admin/configurations" element={<AdminConfigurationsPage />} />
    </Route>
    <Route path="*" element={<StatusPage />} />
  </Route></Routes>
}

