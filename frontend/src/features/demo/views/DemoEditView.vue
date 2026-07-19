<script setup>
import { onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Back, Check } from '@element-plus/icons-vue'
import { useBreakpoint } from '@/shared/composables/useBreakpoint'
import PageLayout from '@/shared/components/PageLayout.vue'
import ContentPanel from '@/shared/components/ContentPanel.vue'
import * as demoApi from '@/features/demo/api'
import { CATEGORIES, STATUSES } from '@/features/demo/data'

const route = useRoute()
const router = useRouter()
const { isMobile, device } = useBreakpoint()

const loading = ref(false)
const found = ref(false)

const form = reactive({
  id: null,
  name: '',
  category: '',
  status: 'ACTIVE',
  price: 0,
  stock: 0,
  owner: '',
  description: '',
  featured: false,
})

function applyItem(item) {
  if (!item) {
    found.value = false
    return
  }
  found.value = true
  Object.assign(form, {
    id: item.id,
    name: item.name,
    category: item.category,
    status: item.status,
    price: item.price,
    stock: item.stock,
    owner: item.owner,
    description: item.description || '',
    featured: !!item.featured,
  })
}

async function load() {
  loading.value = true
  found.value = false
  try {
    if (route.params.id) {
      const { data } = await demoApi.getDemoItem(route.params.id)
      if (data.success) applyItem(data.data)
      return
    }
    const { data } = await demoApi.getDemoItems({ page: 1, size: 1 })
    if (data.success && data.data.items?.length) {
      applyItem(data.data.items[0])
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.message || error.message)
  } finally {
    loading.value = false
  }
}

function goBack() {
  if (form.id) {
    router.push({ name: 'demo-view', params: { id: String(form.id) } })
  } else {
    router.push({ name: 'demo-search' })
  }
}

function handleSave() {
  if (!form.name?.trim()) {
    ElMessage.warning('상품명을 입력해주세요.')
    return
  }
  ElMessage.success('저장되었습니다. (데모 — 실제 DB 반영 없음)')
  router.push({ name: 'demo-view', params: { id: String(form.id) } })
}

watch(() => route.params.id, load)
onMounted(load)
</script>

<template>
  <PageLayout title="수정 화면" :subtitle="`폼 2열 → 모바일 1열 자동 전환 (현재: ${device})`">
    <template #actions>
      <el-button :icon="Back" @click="goBack">취소</el-button>
      <el-button type="primary" :icon="Check" :disabled="!found" @click="handleSave">저장</el-button>
    </template>

    <ContentPanel v-if="found" title="상품 정보" :loading="loading">
      <el-form
        label-position="top"
        class="form-grid"
        @submit.prevent="handleSave"
      >
        <el-form-item label="상품 ID">
          <el-input :model-value="String(form.id)" disabled />
        </el-form-item>
        <el-form-item label="상태">
          <el-radio-group v-model="form.status">
            <el-radio v-for="s in STATUSES" :key="s.value" :value="s.value">
              {{ s.label }}
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="상품명" class="form-grid--full">
          <el-input v-model="form.name" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="카테고리">
          <el-select v-model="form.category" style="width: 100%">
            <el-option v-for="c in CATEGORIES" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>
        <el-form-item label="담당자">
          <el-input v-model="form.owner" />
        </el-form-item>
        <el-form-item label="가격">
          <el-input-number v-model="form.price" :min="0" :step="1000" style="width: 100%" />
        </el-form-item>
        <el-form-item label="재고">
          <el-input-number v-model="form.stock" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="추천">
          <el-checkbox v-model="form.featured">추천 상품</el-checkbox>
        </el-form-item>
        <el-form-item label="설명" class="form-grid--full">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="isMobile ? 4 : 5"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="goBack">취소</el-button>
        <el-button type="primary" @click="handleSave">저장</el-button>
      </template>
    </ContentPanel>

    <ContentPanel v-else :show-header="false" :loading="loading">
      <el-empty v-if="!loading" description="수정할 대상을 찾을 수 없습니다.">
        <el-button type="primary" @click="router.push({ name: 'demo-search' })">검색으로 이동</el-button>
      </el-empty>
    </ContentPanel>
  </PageLayout>
</template>
