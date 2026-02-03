import { defineStore } from 'pinia'
import { ref, computed, watch } from 'vue'

export const useCartStore = defineStore('cart', () => {
  const items = ref<any[]>([])
  
  // Load from localStorage
  const stored = localStorage.getItem('cart')
  if (stored) {
    try { items.value = JSON.parse(stored) } catch {}
  }

  watch(items, (val) => {
    localStorage.setItem('cart', JSON.stringify(val))
  }, { deep: true })

  const totalCount = computed(() => items.value.reduce((sum, item) => sum + item.quantity, 0))
  const totalPrice = computed(() => items.value.reduce((sum, item) => sum + item.price * item.quantity, 0))

  function addToCart(product: any) {
    const existing = items.value.find(i => i.id === product.id)
    if (existing) {
      existing.quantity++
    } else {
      items.value.push({ ...product, quantity: 1 })
    }
  }

  function removeFromCart(productId: number) {
    const idx = items.value.findIndex(i => i.id === productId)
    if (idx > -1) items.value.splice(idx, 1)
  }
  
  function updateQuantity(productId: number, quantity: number) {
    const item = items.value.find(i => i.id === productId)
    if (item) {
      item.quantity = quantity
      if (item.quantity <= 0) removeFromCart(productId)
    }
  }

  function clearCart() {
    items.value = []
  }

  return { items, totalCount, totalPrice, addToCart, removeFromCart, updateQuantity, clearCart }
})
