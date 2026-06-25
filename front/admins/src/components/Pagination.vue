<script setup lang="ts">
import { computed } from 'vue'
import { ArrowLeft, ArrowRight } from '@element-plus/icons-vue'

const props = withDefaults(defineProps<{
  modelValue: number
  total: number
  pageSize?: number
}>(), {
  pageSize: 10,
})

const emit = defineEmits<{
  'update:modelValue': [page: number]
}>()

const totalPages = computed(() => Math.max(1, Math.ceil(props.total / props.pageSize)))

const visiblePages = computed(() => {
  const pages: (number | string)[] = []
  const total = totalPages.value
  const current = props.modelValue

  if (total <= 7) {
    for (let i = 1; i <= total; i++) pages.push(i)
    return pages
  }

  pages.push(1)
  if (current > 3) pages.push('...')

  const start = Math.max(2, current - 1)
  const end = Math.min(total - 1, current + 1)
  for (let i = start; i <= end; i++) pages.push(i)

  if (current < total - 2) pages.push('...')
  pages.push(total)
  return pages
})

function goTo(page: number) {
  if (page >= 1 && page <= totalPages.value) {
    emit('update:modelValue', page)
  }
}
</script>

<template>
  <div v-if="total > pageSize" class="pagination-bar">
    <span class="pg-total">共 {{ total }} 条</span>
    <div class="pg-btns">
      <button class="pg-btn" :disabled="modelValue <= 1" @click="goTo(modelValue - 1)">
        <el-icon><ArrowLeft /></el-icon> 上一页
      </button>
      <template v-for="p in visiblePages" :key="p">
        <span v-if="p === '...'" class="pg-ellipsis">…</span>
        <button v-else class="pg-btn pg-num" :class="{ active: p === modelValue }" @click="goTo(p as number)">
          {{ p }}
        </button>
      </template>
      <button class="pg-btn" :disabled="modelValue >= totalPages" @click="goTo(modelValue + 1)">
        下一页 <el-icon><ArrowRight /></el-icon>
      </button>
    </div>
  </div>
</template>

<style scoped>
.pagination-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 0 6px;
  flex-wrap: wrap;
  gap: 10px;
}

.pg-total {
  font-size: 13px;
  color: #8195aa;
  font-weight: 600;
}

.pg-btns {
  display: flex;
  align-items: center;
  gap: 4px;
}

.pg-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  border: 1px solid #d7e4f0;
  border-radius: 7px;
  background: #fff;
  color: #507391;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all .15s;
}

.pg-btn:hover:not(:disabled):not(.active) {
  background: #f0f5fa;
  border-color: #bccfdf;
}

.pg-btn:disabled {
  opacity: .4;
  cursor: not-allowed;
}

.pg-btn.active {
  color: #fff;
  background: #2467df;
  border-color: #2467df;
}

.pg-num {
  min-width: 34px;
  justify-content: center;
}

.pg-ellipsis {
  width: 34px;
  text-align: center;
  color: #96a8b9;
  font-size: 15px;
  user-select: none;
}
</style>
