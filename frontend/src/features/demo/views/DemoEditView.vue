<script setup>
import { computed, reactive, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useBreakpoint } from '@/shared/composables/useBreakpoint'
import PageLayout from '@/shared/components/PageLayout.vue'
import ContentPanel from '@/shared/components/ContentPanel.vue'
import {
  CATEGORIES,
  DEMO_ITEMS,
  STATUSES,
  findDemoItem,
} from '@/features/demo/data'
import { Back, Check } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const { isMobile, device } = useBreakpoint()

const source = computed(() => {
  if (route.params.id) return findDemoItem(route.params.id)
  return DEMO_ITEMS[0]
})

const form = reactive({
  id: null,
  name: '',
  category: '',
  status: 'ACTIVE',
  price: 0,
  stock: 0,
  owner: '',
  description: '',
})

watch(
  source,
  (item) => {
    if (!item) return
    Object.assign(form, {
      id: item.id,
      name: item.name,
      category: item.category,
      status: item.status,
      price: item.price,
      stock: item.stock,
      owner: item.owner,
      description: item.description,
    })
  },
  { immediate: true },
)

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
</script>

<template>
  <PageLayout title="수정 화면" :subtitle="`폼 2열 → 모바일 1열 자동 전환 (현재: ${device})`">
    <template #actions>
      <el-button :icon="Back" @click="goBack">취소</el-button>
      <el-button type="primary" :icon="Check" @click="handleSave">저장</el-button>
    </template>

    <ContentPanel v-if="source" title="상품 정보" :show-header="true">
      <el-form
        label-position="top"
        class="form-grid"
        :disabled="false"
        @submit.prevent="handleSave"
      >
        <el-form-item label="상품 ID">
          <el-input :model-value="String(form.id)" disabled />
        </el-form-item>
        <el-form-item label="상태">
          <el-select v-model="form.status" style="width: 100%">
            <el-option
              v-for="s in STATUSES"
              :key="s.value"
              :label="s.label"
              :value="s.value"
            />
          </el-select>
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

    <ContentPanel v-else :show-header="false">
      <el-empty description="수정할 대상을 찾을 수 없습니다.">
        <el-button type="primary" @click="router.push({ name: 'demo-search' })">검색으로 이동</el-button>
      </el-empty>
    </ContentPanel>
  </PageLayout>
</template>
