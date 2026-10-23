package com.example.ecomer.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.ecomer.model.Product
import com.example.ecomer.navigation.Screen
import com.example.ecomer.ui.components.ProductImageView
import com.example.ecomer.ui.theme.DeleteRed
import com.example.ecomer.ui.theme.EditOrange
import com.example.ecomer.ui.theme.PrimaryBlue
import com.example.ecomer.utils.ImageUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProductsScreen(navController: NavController) {
    val context = LocalContext.current
    val db = remember { FirebaseFirestore.getInstance() }
    val auth = remember { FirebaseAuth.getInstance() }

    var productList by remember { mutableStateOf<List<Product>>(emptyList()) }
    var isLoadingList by remember { mutableStateOf(true) }

    var tenSanPham by remember { mutableStateOf("") }
    var loaiSanPham by remember { mutableStateOf("") }
    var gia by remember { mutableStateOf("") }
    var imageString by remember { mutableStateOf("") }
    var imageFileName by remember { mutableStateOf("") }

    var editingProductId by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    var productToDelete by remember { mutableStateOf<Product?>(null) }

    // Lắng nghe dữ liệu real-time từ Cloud Firestore
    DisposableEffect(Unit) {
        val listener: ListenerRegistration = db.collection("Products")
            .addSnapshotListener { snapshot, error ->
                isLoadingList = false
                if (error != null) {
                    Toast.makeText(context, "Lỗi tải dữ liệu: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val items = snapshot.documents.map { doc ->
                        Product.fromDocument(doc)
                    }
                    productList = items
                }
            }

        onDispose {
            listener.remove()
        }
    }

    // Bộ chọn hình ảnh từ bộ nhớ thiết bị
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val name = ImageUtils.getFileName(context, uri)
            imageFileName = name
            val base64 = ImageUtils.uriToBase64(context, uri)
            if (base64 != null) {
                imageString = base64
                Toast.makeText(context, "Đã chọn ảnh: $name", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Không thể đọc ảnh, vui lòng thử lại!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun resetForm() {
        tenSanPham = ""
        loaiSanPham = ""
        gia = ""
        imageString = ""
        imageFileName = ""
        editingProductId = null
    }

    fun saveProduct() {
        val nameTrim = tenSanPham.trim()
        val catTrim = loaiSanPham.trim()
        val priceTrim = gia.trim()

        if (nameTrim.isEmpty() || catTrim.isEmpty() || priceTrim.isEmpty()) {
            Toast.makeText(context, "Vui lòng nhập đầy đủ Tên, Loại và Giá sản phẩm!", Toast.LENGTH_SHORT).show()
            return
        }

        isSubmitting = true

        val productData = hashMapOf(
            "tenSanPham" to nameTrim,
            "loaiSanPham" to catTrim,
            "gia" to priceTrim,
            "image" to imageString
        )

        if (editingProductId == null) {
            // Thêm mới sản phẩm
            db.collection("Products")
                .add(productData)
                .addOnSuccessListener {
                    isSubmitting = false
                    Toast.makeText(context, "Thêm sản phẩm thành công!", Toast.LENGTH_SHORT).show()
                    resetForm()
                }
                .addOnFailureListener { e ->
                    isSubmitting = false
                    Toast.makeText(context, "Lỗi thêm sản phẩm: ${e.message}", Toast.LENGTH_LONG).show()
                }
        } else {
            // Cập nhật sản phẩm
            db.collection("Products")
                .document(editingProductId!!)
                .set(productData)
                .addOnSuccessListener {
                    isSubmitting = false
                    Toast.makeText(context, "Cập nhật sản phẩm thành công!", Toast.LENGTH_SHORT).show()
                    resetForm()
                }
                .addOnFailureListener { e ->
                    isSubmitting = false
                    Toast.makeText(context, "Lỗi cập nhật: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Dữ liệu sản phẩm",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color(0xFF263238)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            auth.signOut()
                            Toast.makeText(context, "Đã đăng xuất!", Toast.LENGTH_SHORT).show()
                            navController.navigate(Screen.SignIn.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Đăng xuất",
                            tint = Color.Gray
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = tenSanPham,
                onValueChange = { tenSanPham = it },
                placeholder = { Text("Tên sản phẩm (ví dụ: Ap phong nam)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                singleLine = true,
                shape = RoundedCornerShape(4.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color(0xFF90CAF9),
                    focusedBorderColor = PrimaryBlue
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = loaiSanPham,
                onValueChange = { loaiSanPham = it },
                placeholder = { Text("Loại sản phẩm (ví dụ: Thoi trang nu)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                singleLine = true,
                shape = RoundedCornerShape(4.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color(0xFF90CAF9),
                    focusedBorderColor = PrimaryBlue
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = gia,
                onValueChange = { gia = it },
                placeholder = { Text("Giá sản phẩm (ví dụ: 300000)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(4.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color(0xFF90CAF9),
                    focusedBorderColor = PrimaryBlue
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clickable { imagePickerLauncher.launch("image/*") },
                shape = RoundedCornerShape(4.dp),
                border = BorderStroke(1.dp, Color(0xFF90CAF9)),
                color = Color.White
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = "Chọn ảnh",
                        tint = Color.Black,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (imageFileName.isNotEmpty()) imageFileName
                               else if (imageString.isNotEmpty()) "Đã có ảnh sản phẩm"
                               else "Chọn hình ảnh sản phẩm (pexels-...jpg)",
                        color = if (imageFileName.isNotEmpty() || imageString.isNotEmpty()) Color.Black else Color.Gray,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (imageString.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                imageString = ""
                                imageFileName = ""
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Xóa ảnh đã chọn",
                                tint = Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { saveProduct() },
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(6.dp),
                    enabled = !isSubmitting
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = if (editingProductId == null) "THÊM SẢN PHẨM" else "CẬP NHẬT SẢN PHẨM",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                }

                if (editingProductId != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(
                        onClick = { resetForm() },
                        modifier = Modifier.height(46.dp),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("HỦY", color = Color.Gray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Danh sách sản phẩm:",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(4.dp))

            if (isLoadingList) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryBlue)
                }
            } else if (productList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Chưa có sản phẩm nào. Hãy thêm sản phẩm ở trên!",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(productList, key = { it.id }) { product ->
                        ProductItemCard(
                            product = product,
                            onEditClick = {
                                editingProductId = product.id
                                tenSanPham = product.tenSanPham
                                loaiSanPham = product.loaiSanPham
                                gia = product.gia
                                imageString = product.image
                                imageFileName = if (product.image.isNotEmpty()) "Ảnh hiện tại (${product.tenSanPham})" else ""
                            },
                            onDeleteClick = {
                                productToDelete = product
                            }
                        )
                    }
                }
            }
        }
    }

    // Dialog xác nhận xóa sản phẩm
    if (productToDelete != null) {
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            title = { Text("Xác nhận xóa") },
            text = { Text("Bạn có chắc chắn muốn xóa sản phẩm \"${productToDelete?.tenSanPham}\" không?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val id = productToDelete?.id
                        productToDelete = null
                        if (id != null) {
                            db.collection("Products").document(id)
                                .delete()
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Đã xóa sản phẩm!", Toast.LENGTH_SHORT).show()
                                    if (editingProductId == id) {
                                        resetForm()
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(context, "Lỗi xóa: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
                ) {
                    Text("Xóa", color = DeleteRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { productToDelete = null }) {
                    Text("Hủy")
                }
            }
        )
    }
}

@Composable
fun ProductItemCard(
    product: Product,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, Color(0xFF90CAF9)),
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProductImageView(
                imageData = product.image,
                modifier = Modifier
                    .size(68.dp)
                    .border(BorderStroke(0.5.dp, Color.LightGray))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Tên sp: ${product.tenSanPham}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF263238),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Giá sp: ${product.gia}",
                    fontSize = 13.sp,
                    color = Color(0xFF37474F),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Loại sp: ${product.loaiSanPham}",
                    fontSize = 13.sp,
                    color = Color(0xFF546E7A),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .border(BorderStroke(1.dp, EditOrange), RoundedCornerShape(4.dp))
                        .clickable { onEditClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Sửa",
                        tint = EditOrange,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .border(BorderStroke(1.dp, DeleteRed), RoundedCornerShape(4.dp))
                        .clickable { onDeleteClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Xóa",
                        tint = DeleteRed,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
