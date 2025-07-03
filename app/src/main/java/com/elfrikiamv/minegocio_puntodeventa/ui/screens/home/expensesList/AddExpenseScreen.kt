package com.elfrikiamv.minegocio_puntodeventa.ui.screens.home.expensesList

// AddExpenseScreen.kt

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.elfrikiamv.minegocio_puntodeventa.viewmodel.home.expenses.ExpensesViewModel
import java.util.Locale

@Composable
fun AddExpenseScreen(viewModel: ExpensesViewModel) {
    //val viewModel: ExpensesViewModel = viewModel()

    // Variables para capturar los valores del formulario

    val concept by viewModel.concept
    val paymentMethod by viewModel.paymentMethod
    val description by viewModel.description
    val expensePriceInCents by viewModel.expensePriceInCents
    val expenseAmountGivenInCents by viewModel.expenseAmountGivenInCents
    val errorMessage by viewModel.errorMessage


    /*var concept by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }*/

    // Manejo de enfoque
    val focusManager = LocalFocusManager.current
    val conceptFocusRequester = remember { FocusRequester() }
    //val paymentFocusRequester = remember { FocusRequester() }
    val priceFocusRequester = remember { FocusRequester() }
    val amountFocusRequester = remember { FocusRequester() }
    val descriptionFocusRequester = remember { FocusRequester() }

    //funcion de validacion de descripcion
    fun isValidDescription(input: String): Boolean {
        return input.matches(Regex("^[A-Za-z0-9 ]{0,42}\$"))
    }

    // Función para formatear el precio en centavos a un string con formato 0.00
    fun formatPrice(cents: Long): String {
        return String.format(Locale.US, "%.2f", cents / 100.00)
    }

    // Inicializamos expensePriceInCents a partir del monto del producto
    //var expensePriceInCents by remember { mutableLongStateOf(0L) }

    // Manejo del expense TextField del precio del producto para mantener el cursor al final
    var expensePriceTextFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                formatPrice(expensePriceInCents),
                TextRange(formatPrice(expensePriceInCents).length)
            )
        )
    }
    // Sincronizar cuando expensePriceInCents cambia externamente
    LaunchedEffect(expensePriceInCents) {
        val formatted = formatPrice(expensePriceInCents)
        expensePriceTextFieldValue = TextFieldValue(formatted, TextRange(formatted.length))
    }

    // Inicializamos expenseAmountGivenInCents a partir del monto del producto
    //var expenseAmountGivenInCents by remember { mutableLongStateOf(0L) }

    // Manejo del expense amount given TextField del precio del producto para mantener el cursor al final
    var expenseAmountGivenTextFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                formatPrice(expenseAmountGivenInCents),
                TextRange(formatPrice(expenseAmountGivenInCents).length)
            )
        )
    }
    // Sincronizar cuando expenseAmountGivenInCents cambia externamente
    LaunchedEffect(expenseAmountGivenInCents) {
        val formatted = formatPrice(expenseAmountGivenInCents)
        expenseAmountGivenTextFieldValue = TextFieldValue(formatted, TextRange(formatted.length))
    }

    // Observar el estado de isLoading
    val isLoading by viewModel.isLoading.collectAsState()

    Column(
        modifier = Modifier
            //.padding(paddingValues)
            .fillMaxSize()
        //.padding(16.dp)
    ) {
        // Mostrar el indicador de carga si isLoading es true
        if (isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .padding(end = 16.dp, start = 16.dp, top = 16.dp)
            ) {
                item {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Llena todos los campos para agregar un gasto.",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Campo para el concepto del gasto

                            ConceptoGastoField(
                                concepto = concept,
                                onConceptoChange = { viewModel.concept.value = it },
                                conceptoFocusRequester = conceptFocusRequester,
                                onNext = { priceFocusRequester.requestFocus() }
                            )

                            /*OutlinedTextField(
                                value = concept,
                                onValueChange = { concept = it },
                                label = { Text("Concepto del gasto") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                                isError = concept.isBlank()
                            )*/

                            // Campo para el método de pago

                            val paymentOptions =
                                listOf("Tarjeta", "Efectivo", "Transferencia")
                            Column(modifier = Modifier.selectableGroup()) {
                                Text(
                                    text = "Método de pago:",
                                    //style = MaterialTheme.typography.titleSmall
                                )
                                paymentOptions.forEach { option ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(56.dp)
                                            .selectable(
                                                selected = (option == paymentMethod),
                                                onClick = {
                                                    viewModel.paymentMethod.value = option
                                                },
                                                role = Role.RadioButton
                                            )
                                            .padding(horizontal = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = (option == paymentMethod),
                                            onClick = null // recomendado para accesibilidad
                                        )
                                        Text(
                                            text = option,
                                            //style = MaterialTheme.typography.bodyLarge,
                                            modifier = Modifier.padding(start = 16.dp)
                                        )
                                    }
                                }
                            }
                            /*OutlinedTextField(
                                value = paymentMethod,
                                onValueChange = { paymentMethod = it },
                                label = { Text("Método de pago") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                                isError = paymentMethod.isBlank()
                            )*/

                            // Campo para el monto del gasto

                            OutlinedTextField(
                                value = expensePriceTextFieldValue,
                                onValueChange = { newValue ->
                                    val cleanInput = newValue.text.filter { it.isDigit() }
                                    val newCents = cleanInput.toLongOrNull() ?: 0L
                                    viewModel.expensePriceInCents.longValue =
                                        newCents.coerceAtMost(9999999L) // Límite: 99,999.99

                                    val formatted = formatPrice(expensePriceInCents)
                                    expensePriceTextFieldValue = TextFieldValue(
                                        text = formatted,
                                        selection = TextRange(formatted.length) // cursor al final
                                    )
                                },
                                label = { Text("¿Cuál fue el costo del gasto?") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(priceFocusRequester),
                                //keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Next
                                ),
                                keyboardActions = KeyboardActions(
                                    onNext = { amountFocusRequester.requestFocus() }
                                ),
                                singleLine = true,
                                isError = expensePriceInCents == 0L,
                                supportingText = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        Text("Máximo: $99,999.99")
                                    }
                                }
                            )

                            // Campo para el monto pagado por el gasto

                            OutlinedTextField(
                                value = expenseAmountGivenTextFieldValue,
                                onValueChange = { newValue ->
                                    val cleanInput = newValue.text.filter { it.isDigit() }
                                    val newCents = cleanInput.toLongOrNull() ?: 0L
                                    viewModel.expenseAmountGivenInCents.longValue =
                                        newCents.coerceAtMost(9999999L) // Límite: 99,999.99

                                    val formatted = formatPrice(expenseAmountGivenInCents)
                                    expenseAmountGivenTextFieldValue = TextFieldValue(
                                        text = formatted,
                                        selection = TextRange(formatted.length) // cursor al final
                                    )
                                },
                                label = { Text("¿Cuál fue el monto que pagaste?") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(amountFocusRequester),
                                //keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Next
                                ),
                                keyboardActions = KeyboardActions(
                                    onNext = { descriptionFocusRequester.requestFocus() }
                                ),
                                singleLine = true,
                                isError = expenseAmountGivenInCents == 0L,
                                supportingText = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        Text("Máximo: $99,999.99")
                                    }
                                }
                            )

                            // Campo descripción del gasto

                            OutlinedTextField(
                                value = description,
                                onValueChange = {
                                    if (isValidDescription(it)) {
                                        viewModel.description.value = it
                                    }
                                },
                                label = { Text("Descripción") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(descriptionFocusRequester),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Text,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = { focusManager.clearFocus() }
                                ),
                                isError = description.isBlank(),
                                //singleLine = true,
                                supportingText = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        Text("${description.length}/42")
                                    }
                                }
                            )

                            // mensaje de error

                            if (errorMessage.isNotBlank()) {
                                Text(
                                    text = errorMessage,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                )
                            }
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(72.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConceptoGastoField(
    concepto: String,
    onConceptoChange: (String) -> Unit,
    conceptoFocusRequester: FocusRequester,
    onNext: () -> Unit
) {
    val conceptosSugeridos = listOf(
        "Compra de mercancía",
        "Pago de servicios",
        "Pago de nómina",
        "Mantenimiento",
        "Renta",
        "Publicidad",
        "Papelería y suministros",
        "Transporte",
        "Pago de impuestos",
        "Devolución a cliente",
        "Otros"
    )

    var expanded by remember { mutableStateOf(false) }

    // Filtrar sugerencias según lo que escribe el usuario
    val filteredOptions = conceptosSugeridos.filter {
        it.contains(concepto, ignoreCase = true)
    }

    ExposedDropdownMenuBox(
        expanded = expanded && filteredOptions.isNotEmpty(),
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = concepto,
            onValueChange = {
                if (it.length <= 27) {
                    onConceptoChange(it)
                    expanded = it.isNotBlank()
                }
            },
            label = { Text("Concepto del gasto") },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
                .focusRequester(conceptoFocusRequester),
            isError = concepto.isBlank(),
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            supportingText = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text("${concepto.length}/27")
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { onNext() }
            )
        )

        ExposedDropdownMenu(
            expanded = expanded && filteredOptions.isNotEmpty(),
            onDismissRequest = { expanded = false }
        ) {
            filteredOptions.forEach { selectionOption ->
                DropdownMenuItem(
                    text = { Text(selectionOption) },
                    onClick = {
                        onConceptoChange(selectionOption)
                        expanded = false
                    }
                )
            }
        }
    }
}