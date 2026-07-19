<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useBreakpoint } from '@/shared/composables/useBreakpoint'
import { useI18n } from '@/features/i18n/useI18n'
import PageLayout from '@/shared/components/PageLayout.vue'
import ContentPanel from '@/shared/components/ContentPanel.vue'
import ResponsiveDialog from '@/shared/components/ResponsiveDialog.vue'
import * as demoApi from '@/features/demo/api'
import { formatPrice, statusMeta } from '@/features/demo/data'

const { device, isMobile } = useBreakpoint()
const { tCode } = useI18n()

const basicOpen = ref(false)
const formOpen = ref(false)
const detailOpen = ref(false)
const nestedOpen = ref(false)
const items = ref([])
const loading = ref(false)

const form = reactive({
  title: '',
  memo: '',
})

const selected = ref(null)

async function loadItems() {
  loading.value = true
  try {
    const { data } = await demoApi.getDemoItems({ page: 1, size: 4 })
    if (data.success) {
      items.value = data.data.items || []
      if (!selected.value && items.value.length) {
        selected.value = items.value[0]
      }
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.message || error.message)
  } finally {
    loading.value = false
  }
}

function openBasic() {
  basicOpen.value = true
}

function openForm() {
  form.title = ''
  form.memo = ''
  formOpen.value = true
}

function openDetail(row) {
  selected.value = row
  detailOpen.value = true
}

onMounted(loadItems)

async function openConfirm() {
  try {
    await ElMessageBox.confirm(
      '이 동작은 데모용 확인 팝업입니다. 모바일에서는 폭이 화면에 맞춰집니다.',
      '확인',
      {
        confirmButtonText: '확인',
        cancelButtonText: '취소',
        type: 'warning',
      },
    )
    ElMessage.success('확인했습니다.')
  } catch {
    ElMessage.info('취소했습니다.')
  }
}

function submitForm() {
  if (!form.title.trim()) {
    ElMessage.warning('제목을 입력해주세요.')
    return
  }
  formOpen.value = false
  ElMessage.success(`저장: ${form.title}`)
}
</script>

<template>
  <PageLayout
    title="팝업 테스트"
    :subtitle="`Dialog / MessageBox 반응형 동작 확인 (현재: ${device}${isMobile ? ' · 모바일은 하단 시트' : ''})`"
  >
    <ContentPanel title="팝업 열기">
      <div class="action-grid">
        <el-button type="primary" @click="openBasic">기본 Dialog</el-button>
        <el-button type="success" @click="openForm">폼 Dialog</el-button>
        <el-button type="warning" @click="openConfirm">Confirm MessageBox</el-button>
        <el-button :disabled="items.length < 2" @click="openDetail(items[1])">상세 Dialog</el-button>
      </div>
      <p class="hint">
        창 너비를 줄이거나 개발자 도구 디바이스 모드로 Mobile / Tablet / Desktop을 전환해 보세요.
      </p>
    </ContentPanel>

    <ContentPanel title="목록에서 Dialog 열기" :loading="loading">
      <div class="table-scroll">
        <el-table :data="items" stripe border>
          <el-table-column prop="id" :label="tCode('table', 'id')" width="90" />
          <el-table-column prop="name" :label="tCode('table', 'productName')" min-width="140" />
          <el-table-column :label="tCode('table', 'status')" width="100">
            <template #default="{ row }">
              <el-tag size="small" :type="statusMeta(row.status).type">
                {{ statusMeta(row.status).label }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column :label="tCode('table', 'actions')" width="120" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openDetail(row)">팝업</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </ContentPanel>

    <ResponsiveDialog v-model="basicOpen" title="기본 Dialog" :width="480">
      <p>
        데스크톱·태블릿은 중앙 카드, 모바일은 하단 시트(Bottom Sheet) 형태로 열립니다.
      </p>
      <p class="muted">현재 디바이스: {{ device }}</p>
      <template #footer>
        <el-button @click="basicOpen = false">닫기</el-button>
        <el-button type="primary" @click="basicOpen = false">확인</el-button>
      </template>
    </ResponsiveDialog>

    <ResponsiveDialog v-model="formOpen" title="등록 / 수정 팝업" :width="560">
      <el-form label-position="top" @submit.prevent="submitForm">
        <el-form-item label="제목">
          <el-input v-model="form.title" placeholder="제목" />
        </el-form-item>
        <el-form-item label="메모">
          <el-input v-model="form.memo" type="textarea" :rows="4" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formOpen = false">취소</el-button>
        <el-button type="primary" @click="submitForm">저장</el-button>
        <el-button text @click="nestedOpen = true">중첩 Dialog</el-button>
      </template>
    </ResponsiveDialog>

    <ResponsiveDialog v-model="nestedOpen" title="중첩 Dialog" :width="400">
      <p>부모 Dialog 위에서 열린 중첩 팝업입니다.</p>
      <template #footer>
        <el-button type="primary" @click="nestedOpen = false">닫기</el-button>
      </template>
    </ResponsiveDialog>

    <ResponsiveDialog v-model="detailOpen" title="상세 정보" :width="640">
      <template v-if="selected">
        <el-descriptions :column="isMobile ? 1 : 2" border>
          <el-descriptions-item label="ID">{{ selected.id }}</el-descriptions-item>
          <el-descriptions-item label="상태">
            <el-tag size="small" :type="statusMeta(selected.status).type">
              {{ statusMeta(selected.status).label }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="상품명" :span="2">{{ selected.name }}</el-descriptions-item>
          <el-descriptions-item label="가격">{{ formatPrice(selected.price) }}</el-descriptions-item>
          <el-descriptions-item label="재고">{{ selected.stock }}</el-descriptions-item>
          <el-descriptions-item label="담당자">{{ selected.owner }}</el-descriptions-item>
          <el-descriptions-item label="수정일">{{ selected.updatedAt }}</el-descriptions-item>
          <el-descriptions-item label="설명" :span="2">{{ selected.description }}</el-descriptions-item>
        </el-descriptions>
      </template>
      <template #footer>
        <el-button type="primary" @click="detailOpen = false">닫기</el-button>
      </template>
    </ResponsiveDialog>
  </PageLayout>
</template>

<style scoped>
.action-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
}

@media (max-width: 1023px) {
  .action-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 767px) {
  .action-grid {
    grid-template-columns: 1fr;
  }

  .action-grid .el-button {
    width: 100%;
  }
}

.hint {
  margin: 14px 0 0;
  color: #909399;
  font-size: 13px;
  line-height: 1.5;
}

.muted {
  color: #909399;
  font-size: 13px;
}
</style>
