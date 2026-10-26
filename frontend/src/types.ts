export type Role = 'MEMBER' | 'EDITOR' | 'ADMIN'
export type Visibility = 'PUBLIC' | 'PRIVATE'
export type UsageType = 'GAMING' | 'WORKSTATION' | 'OFFICE' | 'OTHER'
export type ComponentCategory = 'CPU' | 'GPU' | 'MOTHERBOARD' | 'RAM' | 'STORAGE' | 'PSU' | 'CASE' | 'COOLER'
export type StockStatus = 'IN_STOCK' | 'LOW_STOCK' | 'OUT_OF_STOCK'

export interface User {
  id: string
  email: string
  role: Role
}

export interface AuthResponse {
  accessToken: string
  tokenType: 'Bearer'
  expiresIn: number
  user: User
}

export interface Configuration {
  id: string
  ownerId: string
  name: string
  description: string
  budgetEur: number
  usageType: UsageType
  visibility: Visibility
  componentCount: number
  createdAt: string
  updatedAt: string
  _links?: Links
}

export interface Component {
  id: string
  name: string
  category: ComponentCategory
  manufacturer: string
  model: string
  description: string
  powerWatts: number | null
  cheapestOffer?: { priceEur: number; supplierName: string } | null
  offerCount?: number
  createdAt: string
  updatedAt: string
  _links?: Links
}

export interface Offer {
  id: string
  componentId: string
  supplierName: string
  priceEur: number
  productUrl: string
  stockStatus: StockStatus
  checkedAt: string
  createdAt: string
  updatedAt: string
  _links?: Links
}

export interface ConfigurationSummary {
  configuration: Configuration
  components: Array<{ component: Component; cheapestOffer: Offer | null }>
  totalLowestPriceEur: number
  componentsWithoutOffers: number
  remainingBudgetEur: number
  withinBudget: boolean
  _links?: Links
}

export interface PageInfo {
  size: number
  totalElements: number
  totalPages: number
  number: number
}

export interface HalPage<T> {
  _embedded?: Record<string, T[]>
  _links: Links
  page: PageInfo
}

export type Links = Record<string, { href: string }>

export interface ApiProblem {
  title?: string
  detail?: string
  status?: number
  errors?: Record<string, string>
}

export const categoryLabels: Record<ComponentCategory, string> = {
  CPU: 'Procesorius',
  GPU: 'Vaizdo plokštė',
  MOTHERBOARD: 'Pagrindinė plokštė',
  RAM: 'Operatyvioji atmintis',
  STORAGE: 'Duomenų kaupiklis',
  PSU: 'Maitinimo šaltinis',
  CASE: 'Korpusas',
  COOLER: 'Aušinimas',
}

export const usageLabels: Record<UsageType, string> = {
  GAMING: 'Žaidimams', WORKSTATION: 'Darbo stotis', OFFICE: 'Biurui', OTHER: 'Kita',
}

export const stockLabels: Record<StockStatus, string> = {
  IN_STOCK: 'Yra sandėlyje', LOW_STOCK: 'Liko nedaug', OUT_OF_STOCK: 'Nėra sandėlyje',
}

