<template>
  <el-card>
    <template #header>
      <span>특별 사용자 영역</span>
    </template>
    <p>메뉴 Role이 있는 사용자만 접근할 수 있습니다.</p>
    <div style="margin-top: 16px">
      <el-button v-can:read type="info">조회</el-button>
      <el-button v-can:update type="primary">수정</el-button>
      <el-button v-can:delete type="danger">삭제</el-button>
    </div>
  </el-card>
</template>
